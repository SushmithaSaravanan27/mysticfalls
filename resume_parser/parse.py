import re
from typing import List, Optional, Tuple

from resume_parser.base import (
    Resume,
    ContactInfo,
    ExperienceEntry,
    EducationEntry,
    ProjectEntry,
)

_SPACY = None  # lazy-loaded nlp


def _load_spacy():
    global _SPACY
    if _SPACY is not None:
        return _SPACY
    try:
        import spacy

        # Try transformer model first; fallback to small if not available
        try:
            _SPACY = spacy.load("en_core_web_trf")
        except Exception:
            try:
                _SPACY = spacy.load("en_core_web_sm")
            except Exception:
                _SPACY = None
    except Exception:
        _SPACY = None
    return _SPACY


EMAIL_RE = re.compile(r"\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}\b")
PHONE_RE = re.compile(
    r"(?:(?:\+?\d{1,3}[ -]?)?(?:\(?\d{3}\)?|\d{3})[ -]?\d{3}[ -]?\d{4})"
)
URL_RE = re.compile(r"\bhttps?://[^\s)]+", re.IGNORECASE)
LINKEDIN_RE = re.compile(r"(?:linkedin\.com/in/|linkedin\.com/pub/)[^\s)]+", re.IGNORECASE)
GITHUB_RE = re.compile(r"(?:github\.com/)[^\s)]+", re.IGNORECASE)

SECTION_ALIASES = {
    "summary": {"summary", "objective", "profile"},
    "skills": {"skills", "technical skills", "tech skills", "technical skill", "technical"},
    "experience": {
        "experience",
        "work experience",
        "professional experience",
        "employment",
        "employment history",
    },
    "education": {"education", "academics"},
    "projects": {"projects", "selected projects"},
}

DATE_RANGE_RE = re.compile(
    r"(?P<start>(?:\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)[a-z]*\b|\b\d{4}\b)(?:[/\-]\d{4})?)"
    r"\s*(?:–|-|to|—)\s*"
    r"(?P<end>(?:Present|\b(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec)[a-z]*\b|\b\d{4}\b)(?:[/\-]\d{4})?)",
    re.IGNORECASE,
)


SECTION_TITLE_RE = re.compile(
    r"^\s*(?:(?:[A-Z][A-Za-z]+\s+){0,3}[A-Za-z]+)\s*:?$"  # simple heading heuristic
)


def _normalize_lines(text: str) -> List[str]:
    lines = []
    for raw in text.splitlines():
        s = raw.replace("•", "-").replace("–", "-").replace("—", "-").strip()
        s = re.sub(r"\s+", " ", s)
        if s:
            lines.append(s)
    return lines


def _is_section_title(line: str) -> Optional[str]:
    key = line.strip(": ").lower()
    for canonical, aliases in SECTION_ALIASES.items():
        if key in aliases:
            return canonical
    return None


def _split_sections(lines: List[str]) -> dict:
    sections = {"summary": [], "skills": [], "experience": [], "education": [], "projects": []}
    current = None
    for line in lines:
        sec = _is_section_title(line)
        if sec:
            current = sec
            continue
        # Stop skills capture when a new section-like line appears
        if current == "skills" and (_is_section_title(line) or line.isupper() or SECTION_TITLE_RE.match(line)):
            current = None
        if current:
            sections[current].append(line)
    return sections


def _extract_contact(text: str) -> ContactInfo:
    contact = ContactInfo()
    email = EMAIL_RE.search(text)
    if email:
        contact.email = email.group(0)

    # Choose the first plausible phone string
    phone = PHONE_RE.search(text)
    if phone:
        contact.phone = phone.group(0)

    urls = URL_RE.findall(text)
    for u in urls:
        if LINKEDIN_RE.search(u):
            contact.linkedin = u
        elif GITHUB_RE.search(u):
            contact.github = u
        else:
            if not contact.website:
                contact.website = u
    return contact


def _guess_name(header_lines: List[str]) -> str:
    # Prefer spaCy PERSON from first 5 lines
    nlp = _load_spacy()
    if nlp:
        doc = nlp("\n".join(header_lines[:5]))
        persons = [ent.text.strip() for ent in doc.ents if ent.label_ == "PERSON"]
        if persons:
            return persons[0]

    # Fallback: the first line with 2-4 capitalized words
    for line in header_lines[:5]:
        tokens = line.split()
        if 1 <= len(tokens) <= 5 and all(t[0:1].isalpha() for t in tokens):
            # Heuristic: at least two words capitalized
            caps = sum(1 for t in tokens if t[:1].isupper())
            if caps >= 2:
                return line
    return header_lines[0] if header_lines else "Candidate"


def _parse_skills(lines: List[str]) -> List[str]:
    if not lines:
        return []
    # Stop at first line that looks like a new section heading or starts a bullet-less block with many caps
    captured: List[str] = []
    for l in lines:
        if _is_section_title(l) or l.isupper() or SECTION_TITLE_RE.match(l):
            break
        captured.append(l)
    blob = " ".join(captured)
    parts = re.split(r"[|,;/]", blob)
    skills = [p.strip() for p in parts if p.strip()]
    return list(dict.fromkeys(skills))  # unique, preserve order


def _split_experience_entries(lines: List[str]) -> List[List[str]]:
    buckets: List[List[str]] = []
    cur: List[str] = []
    for line in lines:
        if DATE_RANGE_RE.search(line) and cur:
            buckets.append(cur)
            cur = [line]
        else:
            cur.append(line)
    if cur:
        buckets.append(cur)
    return buckets


def _parse_title_company(line: str) -> Tuple[Optional[str], Optional[str]]:
    # Try "Title at Company" or "Title - Company" heuristics
    if " at " in line.lower():
        parts = re.split(r"\bat\b", line, flags=re.IGNORECASE)
        left = parts[0].strip(" -")
        right = parts[1].strip(" -") if len(parts) > 1 else None
        return (left or None, right or None)
    if " - " in line:
        left, right = line.split(" - ", 1)
        return (left.strip() or None, right.strip() or None)
    return (line.strip() or None, None)


def _parse_experience(lines: List[str]) -> List[ExperienceEntry]:
    entries: List[ExperienceEntry] = []
    for bucket in _split_experience_entries(lines):
        title_company: Optional[str] = None
        location: Optional[str] = None
        start: Optional[str] = None
        end: Optional[str] = None
        bullets: List[str] = []

        # Identify date line if present
        date_line_idx = None
        for i, l in enumerate(bucket[:3]):  # search early lines
            m = DATE_RANGE_RE.search(l)
            if m:
                start = m.group("start")
                end = m.group("end")
                date_line_idx = i
                break

        # Title/company likely on the first or the line around date
        header_candidates = []
        if date_line_idx is None:
            header_candidates.append(bucket[0])
        else:
            if date_line_idx > 0:
                header_candidates.append(bucket[date_line_idx - 1])
            header_candidates.append(bucket[date_line_idx + 1] if date_line_idx + 1 < len(bucket) else bucket[0])

        job_title, company = None, None
        for hc in header_candidates:
            jt, co = _parse_title_company(hc)
            if jt and not job_title:
                job_title = jt
            if co and not company:
                company = co

        # Bullets: lines starting with '-' or '*' or that look like statements following header
        for l in bucket:
            if l.startswith("-") or l.startswith("*"):
                bullets.append(l.lstrip("-* ").strip())
        if not bullets:
            # fallback: take remaining lines that are not date or header
            skip = set(header_candidates)
            for l in bucket:
                if l in skip:
                    continue
                if DATE_RANGE_RE.search(l):
                    continue
                bullets.append(l)

        entries.append(
            ExperienceEntry(
                job_title=job_title or "Experience",
                company=company,
                location=location,
                start_date=start,
                end_date=end,
                bullets=[b for b in bullets if b],
            )
        )
    return entries


def _parse_education(lines: List[str]) -> List[EducationEntry]:
    entries: List[EducationEntry] = []
    block: List[str] = []
    flush = lambda blk: entries.append(_edu_from_block(blk)) if blk else None

    for l in lines:
        if DATE_RANGE_RE.search(l) and block:
            flush(block)
            block = [l]
        else:
            block.append(l)
    flush(block)
    return [e for e in entries if e]


def _edu_from_block(lines: List[str]) -> Optional[EducationEntry]:
    if not lines:
        return None
    text = " ".join(lines)
    start, end = None, None
    m = DATE_RANGE_RE.search(text)
    if m:
        start, end = m.group("start"), m.group("end")

    # Heuristic: first line has institution, second has degree
    institution = lines[0]
    degree = None
    field = None
    for l in lines[1:3]:
        if re.search(r"\b(BS|BA|MS|MA|MSc|BSc|PhD|MBA|B\.|M\.)\b", l, re.IGNORECASE) or "Bachelor" in l or "Master" in l:
            degree = l
            break
    details = [l for l in lines[1:] if l != degree]
    return EducationEntry(institution=institution, degree=degree, field_of_study=field, start_date=start, end_date=end, details=details)


def _parse_projects(lines: List[str]) -> List[ProjectEntry]:
    projects: List[ProjectEntry] = []
    current: Optional[ProjectEntry] = None
    for l in lines:
        if l.startswith("-") or l.startswith("*"):
            if current is None:
                current = ProjectEntry(name="Project")
            current.bullets.append(l.lstrip("-* ").strip())
        else:
            if current:
                projects.append(current)
                current = None
            # New project header line, optionally with link in parentheses
            name = l
            link_match = re.search(r"\((https?://[^\s)]+)\)", l)
            link = link_match.group(1) if link_match else None
            projects.append(ProjectEntry(name=name, link=link, bullets=[]))
    if current:
        projects.append(current)
    return projects


def parse_resume_text(text: str) -> Resume:
    lines = _normalize_lines(text)
    # Header guess: before first section title
    header = []
    for l in lines:
        if _is_section_title(l):
            break
        header.append(l)
    name = _guess_name(header)
    contact = _extract_contact("\n".join(header))

    sections = _split_sections(lines)

    summary = None
    if sections["summary"]:
        # Only take until next header-like line to avoid spillover
        captured = []
        for l in sections["summary"]:
            if _is_section_title(l) or l.isupper() or SECTION_TITLE_RE.match(l):
                break
            captured.append(l)
        summary = " ".join(captured) if captured else None

    skills = _parse_skills(sections["skills"])
    experience = _parse_experience(sections["experience"]) if sections["experience"] else []
    education = _parse_education(sections["education"]) if sections["education"] else []
    projects = _parse_projects(sections["projects"]) if sections["projects"] else []

    headline = None
    if len(header) >= 2:
        second = header[1]
        if len(second.split()) <= 8 and not EMAIL_RE.search(second) and not PHONE_RE.search(second):
            headline = second

    return Resume(
        name=name,
        headline=headline,
        summary=summary,
        skills=skills,
        experience=experience,
        education=education,
        projects=projects,
        contact=contact,
    )
