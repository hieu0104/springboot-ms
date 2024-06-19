# Ms
This project is responsible for:
* This work management app project aims to offer a straightforward yet powerful platform for organizing, tracking, and fostering community across various projects and tasks.
* With its user-friendly interface and diverse range of features, users can seamlessly manage workflows, track progress in detail, and collaborate effectively.
# Main feature
* Authentication and authorization systems to ensure secure access using JWT
* Email notifications for keeping users informed with Spring Mail
* Integrated payment functionality for handling transactions using VNPay
* Boards: Create and manage boards to categorize and organize projects, tasks, or workflows. Share boards with team members to collaborate and synchronize work.
* Lists: Create and organize task lists to divide the stages of a project (e.g., To Do, Doing, Done). Easily move and manage task cards between lists.
* Cards: Create task cards to detail each specific task. Attach files, notes, due dates, and labels to organize and track task progress.
* Assign Tasks: Assign and distribute task cards to individual team members for efficient task management. Track progress and receive notifications when tasks are nearing their due dates.
* Due Dates: Set deadlines for each task card to ensure timely completion. Receive notifications and progress updates to meet deadlines.
* Labels: Attach colored labels to task cards for easy categorization and identification of tasks by priority or category.
* Comments and Activity: Discuss and update directly on each task card to share opinions and detailed information. Review activity history to understand changes and feedback.
* Notifications: Receive instant notifications about changes, comments, and activities on boards and task cards. Customize notifications to avoid missing important updates.
* Search and Filters: Quickly search for task cards, lists, and boards. Use filters to display tasks by criteria such as label, due date, or assigned member.

## Tech stack
* Build tool: maven >= 3.9.5
* Java: 21
* Framework: Spring boot 3.2.x
* DBMS: MySQL

## Prerequisites
* Java SDK 21
* A MySQL server

## Start application
`mvn spring-boot:run`

## Build application
`mvn clean package`
