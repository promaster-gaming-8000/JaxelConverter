document.addEventListener("DOMContentLoaded", () => {

    const inputField = document.getElementById("inputPath");
    const outputField = document.getElementById("outputPath");
    const textarea = document.getElementById("customArgsInput");

    function buildCommand() {
        const input = inputField.value;
        const output = outputField.value;

        if (!input || !output) return;

        let cmd = "vengi-voxconvert.exe";

        if (document.getElementById("scale_checkbox").checked) {
            cmd += " --scale 2";
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

    document.getElementById("chooseFileBtn").addEventListener("click", () => {
        javaApp.pickInputFile();
    });

    document.getElementById("chooseFolderBtn").addEventListener("click", () => {
        javaApp.pickOutputFolder();
    });

    document.getElementById("customArgsConvertBtn").addEventListener("click", () => {
        javaApp.convert(textarea.value);
    });

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
            javaApp.selectFormat(ext);
        });
    }

    inputField.addEventListener("input", buildCommand);
    outputField.addEventListener("input", buildCommand);

    window.setFile = function(path) {
        inputField.value = path;
        buildCommand();
    };

    window.setFolder = function(path) {
        outputField.value = path;
        buildCommand();
    };

    window.consoleLog = function(message) {
        const consoleDisplay = document.querySelector(".console_display");
        const line = document.createElement("div");
        line.textContent = message;
        consoleDisplay.appendChild(line);
        consoleDisplay.scrollTop = consoleDisplay.scrollHeight;
    };
});