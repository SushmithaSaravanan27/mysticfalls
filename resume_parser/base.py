from dataclasses import dataclass, field
from typing import List, Optional


@dataclass
class ContactInfo:
    email: Optional[str] = None
    phone: Optional[str] = None
    location: Optional[str] = None
    linkedin: Optional[str] = None
    github: Optional[str] = None
    website: Optional[str] = None


@dataclass
class ExperienceEntry:
    job_title: str
    company: Optional[str] = None
    location: Optional[str] = None
    start_date: Optional[str] = None
    end_date: Optional[str] = None
    bullets: List[str] = field(default_factory=list)


@dataclass
class EducationEntry:
    institution: str
    degree: Optional[str] = None
    field_of_study: Optional[str] = None
    start_date: Optional[str] = None
    end_date: Optional[str] = None
    details: List[str] = field(default_factory=list)


@dataclass
class ProjectEntry:
    name: str
    link: Optional[str] = None
    bullets: List[str] = field(default_factory=list)


@dataclass
class Resume:
    name: str
    headline: Optional[str] = None
    summary: Optional[str] = None
    skills: List[str] = field(default_factory=list)
    experience: List[ExperienceEntry] = field(default_factory=list)
    education: List[EducationEntry] = field(default_factory=list)
    projects: List[ProjectEntry] = field(default_factory=list)
    contact: ContactInfo = field(default_factory=ContactInfo)
