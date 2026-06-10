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
            return;
        }

        setMapMessage(messageElement, "", false);

        var bounds = new window.kakao.maps.LatLngBounds();
        markers.forEach(function (item) {
            var position = new window.kakao.maps.LatLng(item.lat, item.lng);
            var marker = new window.kakao.maps.Marker({
                map: map,
                position: position
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
            return;
        }

        map.setBounds(bounds);
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
