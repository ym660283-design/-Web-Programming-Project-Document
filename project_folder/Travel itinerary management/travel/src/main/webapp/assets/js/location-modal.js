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
