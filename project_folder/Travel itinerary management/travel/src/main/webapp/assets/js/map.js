(function () {

    function escapeHtml(value) {
        return String(value || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function getKakaoApi(callback) {
        if (window.kakao && window.kakao.maps && typeof window.kakao.maps.load === "function") {
            window.kakao.maps.load(function () {
                callback(true);
            });
            return;
        }

        callback(false);
    }

    function readMarkers() {
        return Array.prototype.slice.call(document.querySelectorAll(".trip-map-marker-data"))
            .map(function (element) {
                return {
                    place: element.dataset.place || "",
                    time: element.dataset.time || "",
                    memo: element.dataset.memo || "",
                    cost: element.dataset.cost || "",
                    lat: parseFloat(element.dataset.lat),
                    lng: parseFloat(element.dataset.lng)
                };
            })
            .filter(function (marker) {
                return !Number.isNaN(marker.lat) && !Number.isNaN(marker.lng);
            });
    }

    function setMapMessage(messageElement, text, visible) {
        if (!messageElement) {
            return;
        }

        messageElement.textContent = text;
        messageElement.hidden = !visible;
    }

    function formatCoordinate(value) {
        var number = Number(value);
        if (!isFinite(number)) {
            return "";
        }

        return number.toFixed(7).replace(/0+$/, "").replace(/\.$/, "");
    }

    function toRadians(value) {
        return value * Math.PI / 180;
    }

    function getDistanceKm(from, to) {
        var earthRadiusKm = 6371;
        var latitudeDistance = toRadians(to.lat - from.lat);
        var longitudeDistance = toRadians(to.lng - from.lng);
        var startLatitude = toRadians(from.lat);
        var endLatitude = toRadians(to.lat);
        var a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
            + Math.cos(startLatitude) * Math.cos(endLatitude)
            * Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    function formatDistance(distanceKm) {
        if (distanceKm < 1) {
            return Math.round(distanceKm * 1000) + "m";
        }

        return distanceKm.toFixed(1) + "km";
    }

    function formatMeters(meters) {
        if (!isFinite(meters) || meters <= 0) {
            return "0m";
        }

        if (meters < 1000) {
            return Math.round(meters) + "m";
        }

        return (meters / 1000).toFixed(meters >= 10000 ? 0 : 1) + "km";
    }

    function estimateTravelMinutes(distanceKm) {
        var carSpeedKmh = 30;
        return Math.max(1, Math.round(distanceKm / carSpeedKmh * 60));
    }

    function formatMinutes(minutes) {
        if (minutes < 60) {
            return minutes + "분";
        }

        var hours = Math.floor(minutes / 60);
        var remainingMinutes = minutes % 60;
        return remainingMinutes === 0
            ? hours + "시간"
            : hours + "시간 " + remainingMinutes + "분";
    }

    function formatSeconds(seconds) {
        return formatMinutes(Math.max(1, Math.round(seconds / 60)));
    }

    function parseScheduleMinutes(value) {
        var match = String(value || "").match(/^(\d{1,2}):(\d{2})/);
        if (!match) {
            return null;
        }

        var hours = Number(match[1]);
        var minutes = Number(match[2]);
        if (!isFinite(hours) || !isFinite(minutes)) {
            return null;
        }

        return hours * 60 + minutes;
    }

    function getScheduleGapMinutes(from, to) {
        var fromMinutes = parseScheduleMinutes(from.time);
        var toMinutes = parseScheduleMinutes(to.time);
        if (fromMinutes === null || toMinutes === null || toMinutes < fromMinutes) {
            return null;
        }

        return toMinutes - fromMinutes;
    }

    function addScheduleTiming(leg, travelMinutes) {
        var gapMinutes = getScheduleGapMinutes(leg.from, leg.to);
        leg.gapText = gapMinutes === null ? "" : formatMinutes(gapMinutes);
        leg.isTooTight = gapMinutes !== null && gapMinutes < travelMinutes;
        return leg;
    }

    function initializeTripMap(apiReady) {
        var mapElement = document.getElementById("tripMap");
        if (!mapElement) {
            return;
        }

        var markers = readMarkers();
        var markerCountElement = document.querySelector("[data-trip-map-marker-count]");
        var messageElement = document.querySelector("[data-trip-map-message]");

        if (markerCountElement) {
            markerCountElement.textContent = String(markers.length);
        }

        if (!apiReady || mapElement.dataset.mapReady !== "true") {
            mapElement.classList.add("trip-map-canvas-disabled");
            setMapMessage(messageElement, "Kakao 지도 API를 불러오지 못했습니다. 도메인 등록 또는 카카오맵 활성화 상태를 확인해주세요.", true);
            return;
        }

        var defaultCenter = new window.kakao.maps.LatLng(37.566826, 126.978656);
        var map = new window.kakao.maps.Map(mapElement, {
            center: defaultCenter,
            level: 7
        });

        if (markers.length === 0) {
            centerMapByDestination(map, mapElement.dataset.destination || "");
            setMapMessage(messageElement, "위치가 저장된 세부 일정이 없으면 마커가 표시되지 않습니다.", true);
            renderRouteSummary(markers);
            return;
        }

        setMapMessage(messageElement, "", false);

        var bounds = new window.kakao.maps.LatLngBounds();
        var routePath = markers.map(function (item) {
            return new window.kakao.maps.LatLng(item.lat, item.lng);
        });

        markers.forEach(function (item, index) {
            var position = new window.kakao.maps.LatLng(item.lat, item.lng);
            var marker = new window.kakao.maps.Marker({
                map: map,
                position: position
            });
            new window.kakao.maps.CustomOverlay({
                map: map,
                position: position,
                yAnchor: 1.75,
                content: '<span class="trip-map-order-label">' + (index + 1) + '</span>'
            });

            var content = [
                '<div class="trip-map-info-window">',
                "<strong>", escapeHtml(item.place), "</strong>",
                "<span>", escapeHtml(item.time), " · ", escapeHtml(item.cost), "</span>",
                item.memo ? "<p>" + escapeHtml(item.memo) + "</p>" : "",
                "</div>"
            ].join("");

            var infoWindow = new window.kakao.maps.InfoWindow({
                content: content,
                removable: true
            });

            window.kakao.maps.event.addListener(marker, "click", function () {
                infoWindow.open(map, marker);
            });

            bounds.extend(position);
        });

        if (markers.length === 1) {
            map.setCenter(new window.kakao.maps.LatLng(markers[0].lat, markers[0].lng));
            map.setLevel(4);
            renderRouteSummary(markers);
            return;
        }

        map.setBounds(bounds);
        loadRoadRoute(map, mapElement, markers, routePath, messageElement);
    }

    function drawRoutePolyline(map, path, color, strokeStyle) {
        if (!path || path.length < 2) {
            return null;
        }

        return new window.kakao.maps.Polyline({
            map: map,
            path: path,
            strokeWeight: 4,
            strokeColor: color,
            strokeOpacity: 0.9,
            strokeStyle: strokeStyle || "solid"
        });
    }

    function drawStraightRoute(map, routePath) {
        drawRoutePolyline(map, routePath, "#c4694f", "solid");
    }

    function setBoundsByPath(map, path) {
        if (!path || path.length < 2) {
            return;
        }

        var bounds = new window.kakao.maps.LatLngBounds();
        path.forEach(function (position) {
            bounds.extend(position);
        });
        map.setBounds(bounds);
    }

    function buildStraightRouteSummary(markers) {
        var totalDistance = 0;
        var totalMinutes = 0;
        var legs = [];

        for (var index = 0; index < markers.length - 1; index += 1) {
            var from = markers[index];
            var to = markers[index + 1];
            var distanceKm = getDistanceKm(from, to);
            var minutes = estimateTravelMinutes(distanceKm);

            totalDistance += distanceKm;
            totalMinutes += minutes;
            legs.push(addScheduleTiming({
                from: from,
                to: to,
                distanceText: formatDistance(distanceKm),
                durationText: formatMinutes(minutes)
            }, minutes));
        }

        return {
            totalDistanceText: formatDistance(totalDistance),
            totalDurationText: formatMinutes(totalMinutes),
            legs: legs,
            note: "지도 동선과 이동시간은 저장된 위치 사이의 직선거리 기준 예상값입니다."
        };
    }

    function buildRoadRouteSummary(data, markers) {
        if (!data || data.ok !== true || !Array.isArray(data.segments)) {
            return null;
        }

        var path = [];
        var legs = [];
        var totalDistanceMeters = 0;
        var totalDurationSeconds = 0;

        data.segments.forEach(function (segment, index) {
            var route = segment && segment.routes && segment.routes[0];
            if (!route || route.result_code !== 0) {
                throw new Error("No route");
            }

            var summary = route.summary || {};
            var distanceMeters = Number(summary.distance || 0);
            var durationSeconds = Number(summary.duration || 0);
            var durationMinutes = Math.max(1, Math.ceil(durationSeconds / 60));
            var sections = route.sections || [];

            totalDistanceMeters += distanceMeters;
            totalDurationSeconds += durationSeconds;
            legs.push(addScheduleTiming({
                from: markers[index],
                to: markers[index + 1],
                distanceText: formatMeters(distanceMeters),
                durationText: formatSeconds(durationSeconds)
            }, durationMinutes));

            sections.forEach(function (section) {
                (section.roads || []).forEach(function (road) {
                    var vertexes = road.vertexes || [];
                    for (var pointIndex = 0; pointIndex < vertexes.length - 1; pointIndex += 2) {
                        path.push(new window.kakao.maps.LatLng(vertexes[pointIndex + 1], vertexes[pointIndex]));
                    }
                });
            });
        });

        return {
            path: path,
            totalDistanceText: formatMeters(totalDistanceMeters),
            totalDurationText: formatSeconds(totalDurationSeconds),
            legs: legs,
            note: "지도 동선과 이동시간은 Kakao Mobility 실제 도로 경로 기준입니다."
        };
    }

    function loadRoadRoute(map, mapElement, markers, routePath, messageElement) {
        var routeUrl = mapElement.dataset.routeUrl || "";

        if (!routeUrl || !window.fetch) {
            drawStraightRoute(map, routePath);
            renderRouteSummary(markers);
            return;
        }

        setMapMessage(messageElement, "실제 도로 경로를 불러오는 중입니다.", true);

        window.fetch(routeUrl, {
            headers: {
                "Accept": "application/json"
            }
        })
            .then(function (response) {
                return response.json().then(function (data) {
                    if (!response.ok) {
                        throw new Error(data && data.message ? data.message : "Route failed");
                    }
                    return data;
                });
            })
            .then(function (data) {
                var roadRoute = buildRoadRouteSummary(data, markers);
                if (!roadRoute || !roadRoute.path || roadRoute.path.length < 2) {
                    throw new Error("Empty route");
                }

                drawRoutePolyline(map, roadRoute.path, "#2f6f9f", "solid");
                setBoundsByPath(map, roadRoute.path);
                renderRouteSummary(markers, roadRoute);
                setMapMessage(messageElement, "", false);
            })
            .catch(function () {
                drawStraightRoute(map, routePath);
                renderRouteSummary(markers);
                setMapMessage(messageElement, "실제 도로 경로를 불러오지 못해 직선 동선으로 표시합니다.", true);
            });
    }

    function renderRouteSummary(markers, routeSummary) {
        var summaryElement = document.querySelector("[data-trip-route-summary]");
        var totalElement = document.querySelector("[data-trip-route-total]");
        var legsElement = document.querySelector("[data-trip-route-legs]");
        var noteElement = document.querySelector("[data-trip-route-note]");

        if (!summaryElement || !totalElement || !legsElement) {
            return;
        }

        if (markers.length < 2) {
            summaryElement.hidden = true;
            totalElement.textContent = "";
            legsElement.innerHTML = "";
            return;
        }

        var summary = routeSummary || buildStraightRouteSummary(markers);

        totalElement.textContent = "총 " + summary.totalDistanceText
            + " · 약 " + summary.totalDurationText;
        legsElement.innerHTML = summary.legs.map(function (leg, index) {
            return [
                '<article class="trip-route-leg', leg.isTooTight ? ' needs-change' : '', '">',
                '<span>', index + 1, '</span>',
                '<div>',
                '<strong>', escapeHtml(leg.from.place), ' → ', escapeHtml(leg.to.place), '</strong>',
                '<small>', escapeHtml(leg.from.time), ' 출발 기준 · ',
                escapeHtml(leg.distanceText), ' · 약 ', escapeHtml(leg.durationText),
                leg.gapText ? ' · 일정 간격 ' + escapeHtml(leg.gapText) : '',
                '</small>',
                leg.isTooTight ? '<p class="trip-route-warning">일정의 간격이 이동시간에 비해 짧습니다</p>' : '',
                '</div>',
                '</article>'
            ].join("");
        }).join("");
        if (noteElement) {
            noteElement.textContent = summary.note;
        }
        summaryElement.hidden = false;
    }

    function centerMapByDestination(map, destination) {
        if (!destination || !window.kakao.maps.services) {
            return;
        }

        var places = new window.kakao.maps.services.Places();
        places.keywordSearch(destination, function (results, status) {
            if (status !== window.kakao.maps.services.Status.OK || !results.length) {
                return;
            }

            map.setCenter(new window.kakao.maps.LatLng(results[0].y, results[0].x));
            map.setLevel(6);
        });
    }

    function initializeLocationSearch() {
    }

    window.TravelMap = {
        getKakaoApi: getKakaoApi,
        initializeTripMap: initializeTripMap,
        initializeLocationSearch: initializeLocationSearch
    };
})();

(function () {
    if (!window.TravelMap) {
        return;
    }

    function escapeHtml(value) {
        return String(value || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    }

    function formatCoordinate(value) {
        var number = Number(value);
        if (!isFinite(number)) {
            return "";
        }

        return number.toFixed(7).replace(/0+$/, "").replace(/\.$/, "");
    }

    window.TravelMap.initializeLocationSearch = function (apiReady) {
        var panel = document.querySelector("[data-location-modal-panel]");
        var modal = document.querySelector("[data-location-modal]");

        if (!panel || !modal) {
            return;
        }

        var placeInput = document.getElementById("placeName");
        var latitudeInput = document.getElementById("latitude");
        var longitudeInput = document.getElementById("longitude");
        var openButton = document.getElementById("locationSearchButton");
        var statusElement = panel.querySelector("[data-location-status]");
        var form = panel.closest("form");
        var keywordInput = modal.querySelector("[data-location-modal-keyword]");
        var searchButton = modal.querySelector("[data-location-modal-search]");
        var modalStatusElement = modal.querySelector("[data-location-modal-status]");
        var resultsElement = modal.querySelector("[data-location-modal-results]");
        var mapElement = modal.querySelector("[data-location-modal-map]");
        var selectedElement = modal.querySelector("[data-location-modal-selected]");
        var applyButton = modal.querySelector("[data-location-modal-apply]");

        var pickerMap = null;
        var marker = null;
        var infoWindow = null;
        var geocoder = null;
        var pendingLocation = null;
        var lastFocusedElement = null;

        function setStatus(text, tone) {
            if (!statusElement) {
                return;
            }

            statusElement.textContent = text;
            statusElement.dataset.tone = tone || "";
        }

        function setModalStatus(text, tone) {
            if (!modalStatusElement) {
                return;
            }

            modalStatusElement.textContent = text;
            modalStatusElement.dataset.tone = tone || "";
        }

        function hasSavedLocation() {
            return latitudeInput.value.trim() !== "" && longitudeInput.value.trim() !== "";
        }

        function readSavedLocation() {
            if (!hasSavedLocation()) {
                return null;
            }

            return {
                place: placeInput.value.trim(),
                address: "",
                lat: formatCoordinate(latitudeInput.value),
                lng: formatCoordinate(longitudeInput.value)
            };
        }

        function setSelectedLocation(location) {
            pendingLocation = location;

            if (!selectedElement || !applyButton) {
                return;
            }

            if (!location) {
                selectedElement.textContent = "선택된 위치가 없습니다.";
                applyButton.disabled = true;
                return;
            }

            var title = location.place || location.address || "선택한 지도 위치";
            var detail = location.address && location.address !== title
                ? title + " · " + location.address
                : title;

            selectedElement.textContent = detail + " (" + location.lat + ", " + location.lng + ")";
            applyButton.disabled = false;
        }

        function clearPreview(removeMarker) {
            setSelectedLocation(null);

            if (infoWindow) {
                infoWindow.close();
                infoWindow = null;
            }

            if (removeMarker && marker) {
                marker.setMap(null);
                marker = null;
            }
        }

        function buildInfoWindowContent(location) {
            var title = location.place || location.address || "선택한 지도 위치";
            var description = location.address || "위도 " + location.lat + " · 경도 " + location.lng;

            return [
                '<div class="trip-map-location-popup">',
                "<strong>", escapeHtml(title), "</strong>",
                "<span>", escapeHtml(description), "</span>",
                '<button type="button" data-location-modal-apply-popup>이 위치 사용</button>',
                "</div>"
            ].join("");
        }

        function previewLocation(location) {
            if (!location || !location.lat || !location.lng || !pickerMap) {
                return;
            }

            var position = new window.kakao.maps.LatLng(location.lat, location.lng);
            setSelectedLocation(location);

            pickerMap.setCenter(position);
            if (typeof pickerMap.getLevel === "function" && pickerMap.getLevel() > 4) {
                pickerMap.setLevel(4);
            }

            if (!marker) {
                marker = new window.kakao.maps.Marker({
                    map: pickerMap,
                    position: position
                });
            } else {
                marker.setMap(pickerMap);
                marker.setPosition(position);
            }

            if (infoWindow) {
                infoWindow.close();
            }

            infoWindow = new window.kakao.maps.InfoWindow({
                content: buildInfoWindowContent(location),
                removable: true
            });
            infoWindow.open(pickerMap, marker);
            setModalStatus("선택한 위치를 확인한 뒤 이 위치 사용을 눌러주세요.", "");
        }

        function previewClickedLocation(latLng) {
            var location = {
                place: "",
                address: "",
                lat: formatCoordinate(latLng.getLat()),
                lng: formatCoordinate(latLng.getLng())
            };

            if (!geocoder) {
                previewLocation(location);
                return;
            }

            setModalStatus("선택한 위치의 주소를 확인하고 있습니다.", "");
            geocoder.coord2Address(location.lng, location.lat, function (results, status) {
                var address = "";
                if (status === window.kakao.maps.services.Status.OK && results.length > 0) {
                    address = results[0].road_address ? results[0].road_address.address_name : "";
                    if (!address && results[0].address) {
                        address = results[0].address.address_name;
                    }
                }

                location.address = address;
                previewLocation(location);
            });
        }

        function centerMapByDestination() {
            var destination = panel.dataset.destination || "";
            if (!destination || !window.kakao.maps.services || !pickerMap) {
                return;
            }

            var places = new window.kakao.maps.services.Places();
            places.keywordSearch(destination, function (results, status) {
                if (status !== window.kakao.maps.services.Status.OK || !results.length) {
                    return;
                }

                pickerMap.setCenter(new window.kakao.maps.LatLng(results[0].y, results[0].x));
                pickerMap.setLevel(6);
            });
        }

        function ensurePickerMap(callback) {
            var defaultCenter = new window.kakao.maps.LatLng(37.566826, 126.978656);

            if (!pickerMap) {
                pickerMap = new window.kakao.maps.Map(mapElement, {
                    center: defaultCenter,
                    level: 5
                });

                if (window.kakao.maps.services.Geocoder) {
                    geocoder = new window.kakao.maps.services.Geocoder();
                }

                window.kakao.maps.event.addListener(pickerMap, "click", function (mouseEvent) {
                    previewClickedLocation(mouseEvent.latLng);
                });
            }

            window.setTimeout(function () {
                pickerMap.relayout();
                if (typeof callback === "function") {
                    callback();
                }
            }, 0);
        }

        function renderResults(results) {
            resultsElement.innerHTML = results.map(function (result, index) {
                var address = result.road_address_name || result.address_name || "";

                return [
                    '<button class="schedule-location-result" type="button"',
                    ' data-index="', index, '"',
                    ' data-place="', escapeHtml(result.place_name), '"',
                    ' data-address="', escapeHtml(address), '"',
                    ' data-lat="', escapeHtml(result.y), '"',
                    ' data-lng="', escapeHtml(result.x), '">',
                    "<strong>", escapeHtml(result.place_name), "</strong>",
                    "<span>", escapeHtml(address), "</span>",
                    "</button>"
                ].join("");
            }).join("");

            resultsElement.hidden = false;
            setModalStatus("검색 결과에서 실제 방문 장소를 선택해주세요.", "");

            Array.prototype.slice.call(resultsElement.querySelectorAll(".schedule-location-result"))
                .forEach(function (resultButton) {
                    resultButton.addEventListener("click", function () {
                        previewLocation({
                            place: resultButton.dataset.place || "",
                            address: resultButton.dataset.address || "",
                            lat: formatCoordinate(resultButton.dataset.lat),
                            lng: formatCoordinate(resultButton.dataset.lng)
                        });
                    });
                });
        }

        function buildSearchKeyword(keyword) {
            var destination = panel.dataset.destination || "";
            return destination && keyword.indexOf(destination) === -1
                ? destination + " " + keyword
                : keyword;
        }

        function searchPlaces() {
            var keyword = keywordInput.value.trim();

            if (!keyword) {
                setModalStatus("방문 장소를 먼저 입력해주세요.", "warning");
                keywordInput.focus();
                return;
            }

            clearPreview(true);
            setModalStatus("장소 위치를 검색하고 있습니다.", "");
            resultsElement.hidden = true;
            resultsElement.innerHTML = "";

            var places = new window.kakao.maps.services.Places();
            places.keywordSearch(buildSearchKeyword(keyword), function (results, status) {
                if (status !== window.kakao.maps.services.Status.OK || !results.length) {
                    setModalStatus("검색 결과가 없습니다. 장소명을 조금 더 구체적으로 입력해주세요.", "warning");
                    return;
                }

                renderResults(results.slice(0, 6));
            });
        }

        function closeModal() {
            modal.hidden = true;
            modal.setAttribute("aria-hidden", "true");
            document.body.classList.remove("schedule-location-modal-open");

            if (infoWindow) {
                infoWindow.close();
            }

            if (lastFocusedElement && typeof lastFocusedElement.focus === "function") {
                lastFocusedElement.focus();
            }
        }

        function openModal() {
            lastFocusedElement = document.activeElement;
            modal.hidden = false;
            modal.setAttribute("aria-hidden", "false");
            document.body.classList.add("schedule-location-modal-open");
            keywordInput.value = placeInput.value.trim();
            resultsElement.hidden = true;
            resultsElement.innerHTML = "";
            setModalStatus("검색 결과에서 장소를 선택하거나 지도에서 위치를 클릭하세요.", "");
            clearPreview(true);

            ensurePickerMap(function () {
                var savedLocation = readSavedLocation();
                if (savedLocation) {
                    previewLocation(savedLocation);
                    setModalStatus("현재 저장된 위치입니다. 다른 장소를 검색할 수 있습니다.", "success");
                } else {
                    centerMapByDestination();
                    if (keywordInput.value.trim()) {
                        searchPlaces();
                    }
                }

                keywordInput.focus();
            });
        }

        function applySelection() {
            if (!pendingLocation) {
                return;
            }

            latitudeInput.value = pendingLocation.lat;
            longitudeInput.value = pendingLocation.lng;

            if (pendingLocation.place) {
                placeInput.value = pendingLocation.place;
            } else if (!placeInput.value.trim() && pendingLocation.address) {
                placeInput.value = pendingLocation.address;
            }

            setStatus("위치가 선택되었습니다. 등록하면 DB에 함께 저장됩니다.", "success");
            closeModal();
        }

        function clearLocation() {
            latitudeInput.value = "";
            longitudeInput.value = "";
            clearPreview(true);
            setStatus("장소 위치 선택을 눌러 위치를 선택해주세요.", "");
        }

        function updateInitialStatus() {
            if (hasSavedLocation()) {
                setStatus("저장된 위치 정보가 있습니다.", "success");
                return;
            }

            setStatus("위치를 선택하면 지도 마커에 사용할 위치 정보가 저장됩니다.", "");
        }

        if (!placeInput || !latitudeInput || !longitudeInput || !openButton
                || !keywordInput || !searchButton || !resultsElement
                || !mapElement || !selectedElement || !applyButton) {
            if (openButton) {
                openButton.disabled = true;
            }
            setStatus("장소 위치 선택 창을 초기화하지 못했습니다.", "warning");
            return;
        }

        if (!apiReady || panel.dataset.mapReady !== "true" || !window.kakao.maps.services) {
            openButton.disabled = true;
            setStatus("Kakao 지도 API를 불러오지 못했습니다. 도메인 등록 또는 카카오맵 활성화 상태를 확인해주세요.", "warning");
            return;
        }

        updateInitialStatus();

        openButton.addEventListener("click", openModal);
        searchButton.addEventListener("click", searchPlaces);
        applyButton.addEventListener("click", applySelection);
        placeInput.addEventListener("input", clearLocation);

        keywordInput.addEventListener("keydown", function (event) {
            if (event.key !== "Enter") {
                return;
            }

            event.preventDefault();
            searchPlaces();
        });

        modal.addEventListener("click", function (event) {
            if (event.target.closest("[data-location-modal-close]")) {
                closeModal();
                return;
            }

            if (event.target.closest("[data-location-modal-apply-popup]")) {
                event.preventDefault();
                applySelection();
            }
        });

        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape" && !modal.hidden) {
                closeModal();
            }
        });

        if (form) {
            form.addEventListener("reset", function () {
                window.setTimeout(function () {
                    resultsElement.hidden = true;
                    resultsElement.innerHTML = "";
                    clearPreview(true);
                    updateInitialStatus();
                }, 0);
            });
        }
    };
})();
