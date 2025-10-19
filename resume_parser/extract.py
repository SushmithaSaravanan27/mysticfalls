import io
from typing import IO
from pdfminer.high_level import extract_text as pdf_extract_text
from docx import Document as DocxDocument


def _extract_pdf_text(file_stream: IO[bytes]) -> str:
    file_stream.seek(0)
    text = pdf_extract_text(file_stream)
    return text or ""


def _extract_docx_text(file_stream: IO[bytes]) -> str:
    file_stream.seek(0)
    doc = DocxDocument(file_stream)
    parts = []
    for p in doc.paragraphs:
        if p.text:
            parts.append(p.text)
    for tbl in doc.tables:
        for row in tbl.rows:
            for cell in row.cells:
                cell_text = cell.text.strip()
                if cell_text:
                    parts.append(cell_text)
    return "\n".join(parts)


def extract_text_from_upload(file_stream: IO[bytes], filename: str) -> str:
    name = filename.lower().strip()
    if name.endswith(".pdf"):
        return _extract_pdf_text(file_stream)
    if name.endswith(".docx"):
        return _extract_docx_text(file_stream)
    raise ValueError("Unsupported file type; only PDF and DOCX are supported.")
