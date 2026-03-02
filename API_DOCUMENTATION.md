# API Documentation

## Overview
This document provides detailed information about the REST API endpoints available in the Project Management System.

**Base URL**: `http://localhost:8080`

**Authentication**: Most endpoints require JWT authentication via Bearer token in the Authorization header.

---

## 📑 Table of Contents
1. [Authentication](#authentication)
2. [User Management](#user-management)
3. [Project Management](#project-management)
4. [Issue Management](#issue-management)
5. [Subscription Management](#subscription-management)
6. [Role & Permission Management](#role--permission-management)
7. [Messaging & Comments](#messaging--comments)
8. [Attachments](#attachments)
9. [Invitations](#invitations)

---

## Authentication

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600
}
```

### Logout
```http
POST /auth/logout
Authorization: Bearer {token}
```

---

## User Management

### Create User
```http
POST /users
Content-Type: application/json

{
  "username": "string",
  "password": "string",
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "dob": "2000-01-01"
}
```

**Response**:
```json
{
  "code": 1000,
  "result": {
    "id": "string",
    "username": "string",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "dob": "2000-01-01",
    "roles": []
  }
}
```

### Get Users (with Pagination & Search)
```http
GET /users?page=0&size=10&sort=username,asc&keyword=john
Authorization: Bearer {token}
```

**Query Parameters**:
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)
- `sort` (optional): Sort field and direction (e.g., `username,asc`)
- `keyword` (optional): Search keyword

**Response**:
```json
{
  "code": 1000,
  "result": {
    "content": [
      {
        "id": "string",
        "username": "string",
        "email": "string",
        "firstName": "string",
        "lastName": "string"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10
  }
}
```

### Get User by ID
```http
GET /users/{userId}
Authorization: Bearer {token}
```

### Get My Info
```http
GET /users/my-info
Authorization: Bearer {token}
```

### Update User
```http
PUT /users/{userId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "dob": "2000-01-01"
}
```

### Delete User
```http
DELETE /users/{userId}
Authorization: Bearer {token}
```

---

## Project Management

### Create Project
```http
POST /projects
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "description": "string",
  "category": "string",
  "tags": ["tag1", "tag2"]
}
```

**Response**:
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "category": "string",
  "tags": ["tag1", "tag2"],
  "owner": {
    "id": "string",
    "username": "string"
  },
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### Get Projects
```http
GET /projects?category=web&tag=react
Authorization: Bearer {token}
```

**Query Parameters**:
- `category` (optional): Filter by category
- `tag` (optional): Filter by tag

### Update Project
```http
PATCH /projects/{projectId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "description": "string",
  "category": "string",
  "tags": ["tag1", "tag2"]
}
```

### Delete Project
```http
DELETE /projects/{projectId}
Authorization: Bearer {token}
```

### Search Projects
```http
GET /projects/search?keyword=management
Authorization: Bearer {token}
```

### Get Project Chat
```http
GET /projects/{projectId}/chat
Authorization: Bearer {token}
```

---

## Issue Management

### Create Issue
```http
POST /issues
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "string",
  "description": "string",
  "priority": "HIGH",
  "status": "PENDING",
  "projectId": "string",
  "dueDate": "2024-12-31"
}
```

**Priority Values**: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

**Status Values**: `PENDING`, `IN_PROGRESS`, `DONE`

### Get Issue by ID
```http
GET /issues/{issueId}
Authorization: Bearer {token}
```

### Get Issues by Project
```http
GET /issues/project/{projectId}
Authorization: Bearer {token}
```

### Update Issue
```http
PATCH /issues/{issueId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "string",
  "description": "string",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "dueDate": "2024-12-31"
}
```

### Assign User to Issue
```http
PUT /issues/{issueId}/assign/{userId}
Authorization: Bearer {token}
```

### Delete Issue
```http
DELETE /issues/{issueId}
Authorization: Bearer {token}
```

---

## Subscription Management

### Get User Subscription
```http
GET /subscriptions/user
Authorization: Bearer {token}
```

**Response**:
```json
{
  "id": "string",
  "planType": "FREE",
  "startDate": "2024-01-01",
  "endDate": "2024-12-31",
  "isValid": true
}
```

**Plan Types**: `FREE`, `MONTHLY`, `ANNUAL`

### Upgrade Subscription
```http
POST /subscriptions/upgrade
Authorization: Bearer {token}
Content-Type: application/json

{
  "planType": "MONTHLY",
  "provider": "VNPAY"
}
```

**Providers**: `VNPAY`, `MOMO`

**Response**:
```json
{
  "paymentUrl": "https://payment-gateway.com/pay?token=..."
}
```

---

## Role & Permission Management

### Get All Roles
```http
GET /roles?page=0&size=10
Authorization: Bearer {token}
```

### Create Role
```http
POST /roles
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "description": "string",
  "permissions": ["PERMISSION_1", "PERMISSION_2"]
}
```

### Update Role
```http
PUT /roles/{roleId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "description": "string",
  "permissions": ["PERMISSION_1", "PERMISSION_2"]
}
```

### Delete Role
```http
DELETE /roles/{roleId}
Authorization: Bearer {token}
```

### Get All Permissions
```http
GET /permissions
Authorization: Bearer {token}
```

---

## Messaging & Comments

### Send Message
```http
POST /messages
Authorization: Bearer {token}
Content-Type: application/json

{
  "chatId": "string",
  "content": "string"
}
```

### Get Messages by Chat
```http
GET /messages/chat/{chatId}
Authorization: Bearer {token}
```

### Add Comment
```http
POST /comments
Authorization: Bearer {token}
Content-Type: application/json

{
  "issueId": "string",
  "content": "string"
}
```

### Get Comments by Issue
```http
GET /comments/issue/{issueId}
Authorization: Bearer {token}
```

---

## Attachments

### Upload Attachment
```http
POST /attachments
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: (binary)
projectId: string
```

### Get Attachments by Project
```http
GET /attachments/project/{projectId}
Authorization: Bearer {token}
```

### Delete Attachment
```http
DELETE /attachments/{attachmentId}
Authorization: Bearer {token}
```

---

## Invitations

### Send Project Invitation
```http
POST /projects/invite
Authorization: Bearer {token}
Content-Type: application/json

{
  "email": "user@example.com",
  "projectId": "string"
}
```

### Accept Invitation
```http
GET /projects/accept?token={invitationToken}
Authorization: Bearer {token}
```

---

## Common Response Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 204 | No Content (successful deletion) |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing or invalid token) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found |
| 500 | Internal Server Error |

---

## Error Response Format

```json
{
  "code": 4001,
  "message": "Error description",
  "timestamp": "2024-01-01T00:00:00"
}
```

---

## Pagination Response Format

All paginated endpoints return data in this format:

```json
{
  "code": 1000,
  "result": {
    "content": [...],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false
  }
}
```

---

## Interactive API Documentation

For a complete, interactive API documentation with request/response examples, visit:

**Swagger UI**: http://localhost:8080/swagger-ui.html

This provides:
- Try-it-out functionality
- Complete schema definitions
- Request/response examples
- Authentication testing
