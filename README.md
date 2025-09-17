# Trackfolio – Placement Drive Tracker

**Trackfolio** is a centralized platform that helps students organize and manage their placement prep. It consolidates *
*drives**, **notes**, **checklists**, and **job descriptions (JDs)** in one place, with an **AI-powered assistant**
providing context-aware guidance based on uploaded JDs and user skills.

This repository contains the **backend**, built with **Spring Boot** and **PostgreSQL**, with a microservice handling *
*AI chatbot responses**.

---

## Features

### Secure Multi-User System

- **JWT authentication** with access and refresh tokens
- Provides strict per-user data isolation

### Database Management

- Normalized **PostgreSQL** schema
- Ownership checks, drive categorization, and dynamic filtering for efficient querying

### Job Description Automation

- **Apache PDFBox** pipeline parses PDFs and indexes text
- Makes JDs searchable and reduces manual effort

### AI Integration

- **Spring Boot microservice** that fetches AI-generated responses for placement prep queries
- https://github.com/adithya230o/ai-core-microservice

### Full-Stack Collaboration

- Backend APIs integrated with a **Next.js frontend** for complete user workflow
- https://github.com/jithyavaishnavi/Trackfolio-Frontend

---

## Tech Stack

- **Backend:** Spring Boot, Java 17
- **Database:** PostgreSQL
- **AI Microservice:** Spring Boot
- **PDF Parsing:** Apache PDFBox
- **Frontend Integration:** Next.js

---

## Demo

See the live platform in action: https://trackfolio-dusky.vercel.app

---

## Architecture Overview

- **Spring Boot:** Handles authentication, drive management, note/checklist CRUD, JD parsing, and AI microservice
  integration
- **PostgreSQL:** Stores all user data with strict isolation and indexing for efficient queries
- **AI Microservice:** Fetches AI-generated replies for prep guidance
- **PDF Pipeline:** Converts uploaded JDs into searchable text, improving discoverability
- **Frontend Integration:** REST APIs with Next.js for end-to-end user workflow

---
