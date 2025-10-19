import io
import re
import uuid
from datetime import datetime
from flask import Flask, render_template, request, abort, send_file, jsonify
from werkzeug.utils import secure_filename

from resume_parser.extract import extract_text_from_upload
from resume_parser.parse import parse_resume_text
from resume_parser.templates.classic import generate_classic_docx
from resume_parser.templates.modern import generate_modern_docx
from resume_parser.templates.tabular import generate_tabular_docx
from resume_parser.templates.sidebar import generate_sidebar_docx
from resume_parser.templates.minimal import generate_minimal_docx

ALLOWED_EXTENSIONS = {"pdf", "docx"}

app = Flask(__name__, template_folder="templates", static_folder="static")
app.config["MAX_CONTENT_LENGTH"] = 10 * 1024 * 1024  # 10 MB

# In-memory storage for converted files
TEMP_STORAGE = {}


def allowed_file(filename: str) -> bool:
    return "." in filename and filename.rsplit(".", 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route("/", methods=["GET"])
def index():
    return render_template("index.html")


@app.route("/convert", methods=["POST"])
def convert():
    if "resume" not in request.files:
        abort(400, description="No file part named 'resume'")
    file = request.files["resume"]
    if file.filename == "":
        abort(400, description="No file selected")
    if not allowed_file(file.filename):
        abort(415, description="Unsupported file type. Use PDF or DOCX.")

    chosen_template = (request.form.get("template", "modern") or "modern").lower()
    if chosen_template not in {"modern", "classic", "tabular", "sidebar", "minimal"}:
        abort(400, description="Unknown template. Choose 'modern', 'classic', 'tabular', 'sidebar', or 'minimal'.")

    filename = secure_filename(file.filename)
    try:
        text = extract_text_from_upload(file.stream, filename)
    except Exception as ex:
        abort(422, description=f"Failed to extract text: {ex}")

    try:
        resume = parse_resume_text(text)
    except Exception as ex:
        abort(422, description=f"Failed to parse resume: {ex}")

    try:
        if chosen_template == "modern":
            output = generate_modern_docx(resume)
            tpl_name = "Modern"
        elif chosen_template == "classic":
            output = generate_classic_docx(resume)
            tpl_name = "Classic"
        elif chosen_template == "tabular":
            output = generate_tabular_docx(resume)
            tpl_name = "Tabular"
        elif chosen_template == "sidebar":
            output = generate_sidebar_docx(resume)
            tpl_name = "Sidebar"
        else:
            output = generate_minimal_docx(resume)
            tpl_name = "Minimal"
    except Exception as ex:
        abort(500, description=f"Failed to generate DOCX: {ex}")

    suggested_name = resume.name or "Candidate"
    suggested_name = re.sub(r"[^A-Za-z0-9_-]+", "_", suggested_name).strip("_")
    ts = datetime.now().strftime("%Y%m%d")
    download_name = f"{suggested_name}_{tpl_name}_{ts}.docx"

    # Store in memory with a unique ID
    file_id = str(uuid.uuid4())
    TEMP_STORAGE[file_id] = {
        "filename": download_name,
        "content": output.getvalue(),
    }

    return jsonify({"status": "success", "file_id": file_id, "filename": download_name})


@app.route("/download/<file_id>", methods=["GET"])
def download(file_id):
    file_data = TEMP_STORAGE.get(file_id)
    if not file_data:
        abort(404, description="File not found or expired")

    return send_file(
        io.BytesIO(file_data["content"]),
        mimetype="application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        as_attachment=True,
        download_name=file_data["filename"],
        max_age=0,
    )


if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)
