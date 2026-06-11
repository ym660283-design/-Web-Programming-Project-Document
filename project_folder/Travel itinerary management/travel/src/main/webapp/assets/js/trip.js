(function () {
    function initializeScheduleDateSelection() {
        var scheduleDateSelect = document.querySelector("[data-schedule-date-select]");
        var manageList = document.querySelector("[data-schedule-manage-list]");

        if (!scheduleDateSelect || !manageList) {
            return;
        }

        function updateScheduleManageList() {
            var selectedOption = scheduleDateSelect.options[scheduleDateSelect.selectedIndex];
            var selectedDay = selectedOption ? selectedOption.getAttribute("data-day") : "";
            var selectedPanel = manageList.querySelector(
                "[data-schedule-day-panel='" + selectedDay + "']"
            );
            var heading = manageList.querySelector("[data-schedule-manage-heading]");
            var count = manageList.querySelector("[data-schedule-manage-count]");

            if (!selectedDay || !selectedPanel || !heading || !count) {
                return;
            }

            manageList.querySelectorAll("[data-schedule-day-panel]").forEach(function (panel) {
                panel.hidden = panel !== selectedPanel;
            });

            heading.textContent = "DAY " + selectedDay + " 등록 일정 관리";
            count.textContent = selectedPanel.getAttribute("data-schedule-count") + "개 일정";
        }

        scheduleDateSelect.addEventListener("change", updateScheduleManageList);
        scheduleDateSelect.form.addEventListener("reset", function () {
            window.setTimeout(updateScheduleManageList, 0);
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        initializeScheduleDateSelection();

        if (!window.TravelMap) {
            return;
        }

        window.TravelMap.getKakaoApi(function (apiReady) {
            window.TravelMap.initializeTripMap(apiReady);
            window.TravelMap.initializeLocationSearch(apiReady);
        });
    });
})();
