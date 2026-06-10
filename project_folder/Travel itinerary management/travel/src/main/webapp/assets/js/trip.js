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

    function initializeLocationSearch(apiReady) {
        var panel = document.querySelector("[data-location-panel]");
        if (!panel) {
            return;
        }

        var placeInput = document.getElementById("placeName");
        var latitudeInput = document.getElementById("latitude");
        var longitudeInput = document.getElementById("longitude");
        var button = document.getElementById("locationSearchButton");
        var statusElement = panel.querySelector("[data-location-status]");
        var resultsElement = panel.querySelector("[data-location-results]");
        var form = panel.closest("form");

        function setStatus(text, tone) {
            statusElement.textContent = text;
            statusElement.dataset.tone = tone || "";
        }

        function clearLocation() {
            latitudeInput.value = "";
            longitudeInput.value = "";
            setStatus("장소 위치 찾기를 눌러 위치를 선택해주세요.", "");
        }

        function hasSavedLocation() {
            return latitudeInput.value.trim() !== "" && longitudeInput.value.trim() !== "";
        }

        function updateInitialStatus() {
            if (hasSavedLocation()) {
                setStatus("저장된 위치 정보가 있습니다.", "success");
                return;
            }

            setStatus("위치를 선택하면 지도 마커에 사용할 위치 정보가 저장됩니다.", "");
        }

        if (!apiReady || panel.dataset.mapReady !== "true" || !window.kakao.maps.services) {
            button.disabled = true;
            setStatus("Kakao 지도 API를 불러오지 못했습니다. 도메인 등록 또는 카카오맵 활성화 상태를 확인해주세요.", "warning");
            return;
        }

        updateInitialStatus();

        placeInput.addEventListener("input", clearLocation);

        if (form) {
            form.addEventListener("reset", function () {
                setTimeout(function () {
                    resultsElement.hidden = true;
                    resultsElement.innerHTML = "";
                    updateInitialStatus();
                }, 0);
            });
        }

        button.addEventListener("click", function () {
            var keyword = placeInput.value.trim();
            var destination = panel.dataset.destination || "";

            if (!keyword) {
                setStatus("방문 장소를 먼저 입력해주세요.", "warning");
                placeInput.focus();
                return;
            }

            var searchKeyword = destination && keyword.indexOf(destination) === -1
                ? destination + " " + keyword
                : keyword;

            setStatus("장소 위치를 검색하고 있습니다.", "");
            resultsElement.hidden = true;
            resultsElement.innerHTML = "";

            var places = new window.kakao.maps.services.Places();
            places.keywordSearch(searchKeyword, function (results, searchStatus) {
                if (searchStatus !== window.kakao.maps.services.Status.OK || !results.length) {
                    setStatus("검색 결과가 없습니다. 장소명을 조금 더 구체적으로 입력해주세요.", "warning");
                    return;
                }

                renderLocationResults(results.slice(0, 5));
            });
        });

        function renderLocationResults(results) {
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
            setStatus("검색 결과에서 실제 방문 장소를 선택해주세요.", "");

            Array.prototype.slice.call(resultsElement.querySelectorAll(".schedule-location-result"))
                .forEach(function (resultButton) {
                    resultButton.addEventListener("click", function () {
                        placeInput.value = resultButton.dataset.place || placeInput.value;
                        latitudeInput.value = resultButton.dataset.lat || "";
                        longitudeInput.value = resultButton.dataset.lng || "";
                        resultsElement.hidden = true;
                        setStatus("위치가 선택되었습니다. 등록하면 DB에 함께 저장됩니다.", "success");
                    });
                });
        }
    }

    document.addEventListener("DOMContentLoaded", function () {
        getKakaoApi(function (apiReady) {
            initializeTripMap(apiReady);
            initializeLocationSearch(apiReady);
        });
    });
})();
