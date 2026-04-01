const formatButtons = document.querySelectorAll(".slct_file_scroll button");

document.addEventListener("DOMContentLoaded", () => {

    // =========================
    // SIDEBAR NAVIGATION (UNCHANGED)
    // =========================
    document.getElementById("converter_side_buttons").addEventListener("click", () => {
        document.querySelector('.Converter_info').style.display = "block";
        document.querySelector('.help_section').style.display = "none";
        document.querySelector('.credits_section').style.display = "none";

        document.querySelector('#converter_side_buttons').style.backgroundColor = "rgb(83, 83, 83)";
        document.querySelector('#help_side_buttons').style.backgroundColor = "rgb(29, 29, 29)";
        document.querySelector('#credits_side_buttons').style.backgroundColor = "rgb(29, 29, 29)";
    });

    document.getElementById("help_side_buttons").addEventListener("click", () => {
        document.querySelector('.Converter_info').style.display = "none";
        document.querySelector('.help_section').style.display = "block";
        document.querySelector('.credits_section').style.display = "none";

        document.querySelector('#converter_side_buttons').style.backgroundColor = "rgb(29, 29, 29)";
        document.querySelector('#help_side_buttons').style.backgroundColor = "rgb(83, 83, 83)";
        document.querySelector('#credits_side_buttons').style.backgroundColor = "rgb(29, 29, 29)";
    });

    document.getElementById("credits_side_buttons").addEventListener("click", () => {
        document.querySelector('.Converter_info').style.display = "none";
        document.querySelector('.help_section').style.display = "none";
        document.querySelector('.credits_section').style.display = "block";

        document.querySelector('#converter_side_buttons').style.backgroundColor = "rgb(29, 29, 29)";
        document.querySelector('#help_side_buttons').style.backgroundColor = "rgb(29, 29, 29)";
        document.querySelector('#credits_side_buttons').style.backgroundColor = "rgb(83, 83, 83)";
    });

    // =========================
    // ELEMENTS
    // =========================
    const inputField = document.getElementById("inputPath");
    const outputField = document.getElementById("outputPath");
    const textarea = document.getElementById("customArgsInput");

    // =========================
    // BUILD COMMAND (FIXED)
    // =========================
    function buildCommand() {
        const input = inputField.value;
        const output = outputField.value;

        if (!input || !output) return;

        let cmd = "vengi-voxconvert.exe";

        if (document.getElementById("scale_checkbox").checked) {
            cmd += " --scale 2"; // FIXED (scale was undefined)
        }

        if (document.getElementById("force_checkbox").checked) {
            cmd += " --force";
        }

        if (document.getElementById("surface_only_checkbox").checked) {
            cmd += " --surface-only";
        }

        if (document.getElementById("export_palette_checkbox").checked) {
            cmd += " --export-palette";
        }

        if (document.getElementById("json_checkbox").checked) {
            cmd += " --json";
        }

        cmd += ` --input "${input}" --output "${output}"`;

        textarea.value = cmd;
    }

    const autoElements = [
        "scale_checkbox",
        "force_checkbox",
        "surface_only_checkbox",
        "export_palette_checkbox",
        "json_checkbox"
    ];

    autoElements.forEach(id => {
        document.getElementById(id).addEventListener("change", buildCommand);
    });

    inputField.addEventListener("input", buildCommand);
    outputField.addEventListener("input", buildCommand);

    // =========================
    // BLACKOUT LOGIC (UNCHANGED)
    // =========================
    document.getElementById("scale_checkbox").addEventListener("click", () => {
        document.querySelector(".scale_blckout").style.display =
            document.getElementById("scale_checkbox").checked ? "none" : "flex";
    });

    document.getElementById("force_checkbox").addEventListener("click", () => {
        document.querySelector(".force_blckout").style.display =
            document.getElementById("force_checkbox").checked ? "none" : "flex";
    });

    document.getElementById("surface_only_checkbox").addEventListener("click", () => {
        document.querySelector(".surface_only_blckout").style.display =
            document.getElementById("surface_only_checkbox").checked ? "none" : "flex";
    });

    document.getElementById("export_palette_checkbox").addEventListener("click", () => {
        document.querySelector(".export_palette_blckout").style.display =
            document.getElementById("export_palette_checkbox").checked ? "none" : "flex";
    });

    document.getElementById("json_checkbox").addEventListener("click", () => {
        document.querySelector(".json_blckout").style.display =
            document.getElementById("json_checkbox").checked ? "none" : "flex";
    });

    // =========================
    // FILE BUTTONS (FIXED)
    // =========================
    document.getElementById("chooseFileBtn").addEventListener("click", () => {
        javaApp.pickInputFile();
    });

    document.getElementById("chooseFolderBtn").addEventListener("click", () => {
        javaApp.pickOutputFolder();
    });

    // =========================
    // FORMAT BUTTONS (FIXED)
    // =========================
    const formatMap = {
        "slct_obj_file": "obj",
        "slct_vox_file": "vox",
        "slct_vxl_file": "vxl",
        "slct_qb_file": "qb",
        "slct_binvox_file": "binvox",
        "slct_kv6_file": "kv6",
        "slct_v3a_file": "v3a",
        "slct_fbx_file": "fbx",
        "slct_gltf_file": "gltf",
        "slct_stl_file": "stl",
        "slct_ply_file": "ply"
    };

    for (const [id, ext] of Object.entries(formatMap)) {
        document.getElementById(id).addEventListener("click", () => {

            let currentOutput = outputField.value;

            if (currentOutput) {
                currentOutput = currentOutput.replace(/\.\w+$/, "." + ext);
                outputField.value = currentOutput;
                buildCommand();
            }

            // 🔥 REPLACED navigation with Java bridge
            javaApp.selectFormat(ext);
        });
    }

    // =========================
    // CONVERT BUTTON (FIXED)
    // =========================
    document.getElementById("customArgsConvertBtn").addEventListener("click", () => {
        const customArgs = textarea.value;
        javaApp.convert(customArgs);
    });

    // =========================
    // JAVA → JS FUNCTIONS
    // =========================
    window.setFile = function(path) {
        inputField.value = path;
        window.updateFormatButtons();
    };

    window.setFolder = function(path) {
        outputField.value = path;
        window.updateFormatButtons();
    };

    function updateButtonStyles() {
        let anyDisabled = false;

        formatButtons.forEach(btn => {
            if (btn.disabled) {
                btn.style.cursor = "not-allowed";
                anyDisabled = true;
            } else {
                btn.style.cursor = "pointer";
            }
        });

        document.querySelector('.file_blckout').style.display = anyDisabled ? "flex" : "none";
        document.querySelector('.more_settings_blckout').style.display = anyDisabled ? "flex" : "none";
    }

    window.updateFormatButtons = function() {
        const hasInput = inputField.value.trim() !== "";
        const hasOutput = outputField.value.trim() !== "";

        const enable = hasInput && hasOutput;

        formatButtons.forEach(btn => btn.disabled = !enable);

        updateButtonStyles();

        buildCommand();
    };

    inputField.addEventListener("input", window.updateFormatButtons);
    outputField.addEventListener("input", window.updateFormatButtons);

    // =========================
    // CONSOLE
    // =========================
    window.consoleLog = function(message) {
        const consoleDisplay = document.querySelector(".console_display");
        const line = document.createElement("div");
        line.textContent = message;
        consoleDisplay.appendChild(line);
        consoleDisplay.scrollTop = consoleDisplay.scrollHeight;
    };

});