document.addEventListener("DOMContentLoaded", function () {
    var viewDayList = document.getElementById("scheduleViewDayList");
    var dayButtons = viewDayList.querySelectorAll("[data-schedule-day]");
    var timelineHeading = document.getElementById("timelineHeading");
    var scheduleCount = document.getElementById("scheduleCount");
    var timeline = document.querySelector("[data-day-panel='1']");
    var emptyDay = document.querySelector("[data-empty-day]");
    var emptyDayHeading = document.getElementById("emptyDayHeading");
    var manageHeading = document.getElementById("scheduleManageListHeading");
    var manageCount = document.getElementById("manageScheduleCount");
    var manageItems = document.querySelector("[data-manage-day-panel='1']");
    var manageEmpty = document.querySelector("[data-manage-empty]");
    var manageEmptyHeading = document.getElementById("manageEmptyHeading");
    var manageDayList = document.getElementById("scheduleManageDayList");
    var formDayButtons = manageDayList.querySelectorAll("[data-form-schedule-day]");
    var formDateInput = document.getElementById("scheduleDate");
    var selectedManageDay = document.getElementById("selectedManageDay");
    var selectedManageDate = document.getElementById("selectedManageDate");
    var detailIdInput = document.getElementById("scheduleDetailId");
    var submitButton = document.getElementById("scheduleSubmitButton");
    var formNote = document.getElementById("scheduleFormNote");
    var formResetButton = document.getElementById("scheduleFormReset");
    var editCancelButton = document.getElementById("scheduleEditCancel");
    var placeNameInput = document.getElementById("placeName");
    var visitTimeInput = document.getElementById("visitTime");
    var costInput = document.getElementById("cost");
    var memoInput = document.getElementById("memo");
    var editButtons = document.querySelectorAll("[data-edit-schedule]");

    function selectManageDay(day) {
        var selectedButton = manageDayList.querySelector(
            "[data-form-schedule-day='" + day + "']"
        );

        if (selectedButton) {
            selectedButton.click();
        }
    }

    function setCreateMode() {
        detailIdInput.value = "";
        submitButton.textContent = "세부 일정 등록";
        editCancelButton.classList.add("d-none");
        formNote.textContent = "현재는 화면 예시이며 입력 내용은 저장되지 않습니다.";
    }

    function clearScheduleFields() {
        placeNameInput.value = "";
        visitTimeInput.value = "";
        costInput.value = "";
        memoInput.value = "";
        clearRequiredState(placeNameInput);
        clearRequiredState(visitTimeInput);
    }

    function clearRequiredState(input) {
        input.classList.remove("is-invalid");
        input.removeAttribute("aria-invalid");
    }

    function validateRequiredInput(input) {
        var isValid = input.value.trim() !== "";

        input.classList.toggle("is-invalid", !isValid);
        if (isValid) {
            input.removeAttribute("aria-invalid");
        } else {
            input.setAttribute("aria-invalid", "true");
        }

        return isValid;
    }

    dayButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            var day = button.dataset.scheduleDay;

            dayButtons.forEach(function (item) {
                var isSelected = item === button;
                item.classList.toggle("active", isSelected);
                item.setAttribute("aria-selected", String(isSelected));
            });

            timelineHeading.textContent = "DAY " + day + " 일정";

            var hasSampleSchedule = day === "1";
            timeline.classList.toggle("d-none", !hasSampleSchedule);
            emptyDay.classList.toggle("d-none", hasSampleSchedule);
            scheduleCount.textContent = hasSampleSchedule ? "3개 일정" : "0개 일정";
            emptyDayHeading.textContent = "DAY " + day + " 일정이 비어 있습니다";
        });
    });

    formDayButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            var day = button.dataset.formScheduleDay;
            var hasSampleSchedule = day === "1";

            formDayButtons.forEach(function (item) {
                var isSelected = item === button;
                item.classList.toggle("active", isSelected);
                item.setAttribute("aria-selected", String(isSelected));
            });

            formDateInput.value = button.dataset.formScheduleDate;
            selectedManageDay.textContent = "DAY " + day;
            selectedManageDate.textContent = button.dataset.formScheduleLabel;
            manageHeading.textContent = "DAY " + day + " 등록 일정 관리";
            manageItems.classList.toggle("d-none", !hasSampleSchedule);
            manageEmpty.classList.toggle("d-none", hasSampleSchedule);
            manageCount.textContent = hasSampleSchedule ? "3개 일정" : "0개 일정";
            manageEmptyHeading.textContent = "DAY " + day + "에 등록된 일정이 없습니다";
        });
    });

    editButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            selectManageDay(button.dataset.day);

            detailIdInput.value = button.dataset.detailId;
            placeNameInput.value = button.dataset.place;
            visitTimeInput.value = button.dataset.time;
            costInput.value = button.dataset.cost;
            memoInput.value = button.dataset.memo;
            clearRequiredState(placeNameInput);
            clearRequiredState(visitTimeInput);
            submitButton.textContent = "세부 일정 수정";
            editCancelButton.classList.remove("d-none");
            formNote.textContent = button.dataset.place + " 일정을 수정하고 있습니다.";
            placeNameInput.focus();
        });
    });

    [placeNameInput, visitTimeInput].forEach(function (input) {
        input.addEventListener("input", function () {
            if (input.value.trim() !== "") {
                clearRequiredState(input);
            }
        });
    });

    submitButton.addEventListener("click", function () {
        var isPlaceValid = validateRequiredInput(placeNameInput);
        var isTimeValid = validateRequiredInput(visitTimeInput);

        if (!isPlaceValid) {
            placeNameInput.focus();
            return;
        }

        if (!isTimeValid) {
            visitTimeInput.focus();
        }
    });

    formResetButton.addEventListener("click", function () {
        clearScheduleFields();
        placeNameInput.focus();
    });

    editCancelButton.addEventListener("click", function () {
        clearScheduleFields();
        setCreateMode();
        placeNameInput.focus();
    });
});
