## Gateway Service
- API gateway for routing requests to appropriate services

## User Service
- User registration, authentication, profile management
- Database: `mongoDB` (for flexible user profiles)

### Database Schema
```json
users
{
  "id": "UUID",
  "first_name": "String",
  "last_name": "String",
  "email": "String",
  "email_verified": "Boolean",
  "password_hash": "String",
  "profile_image_url": "String",
  "role": "Enum('student', 'instructor', 'admin')",
  "date_of_birth": "Date",
  "language_preference": "String",
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}

instructors
{
  "id": "UUID",
  "user_id": "UUID (Reference to users.id)",
  "bio": "String",
  "expertise":  ["String"],
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}
```

## Course Service
- Course creation, updates, deletions
- Lesson management
- Course enrollment
- Learning progress tracking, completion status
- Database: `MongoDB` (for flexible content structure)

### Database Schema
```json
courses
{
  "id": "UUID",
  "title": "String",
  "description": "String",
  "instructor_id": "UUID (Reference to instructors.id)",
  "category": "String",
  "tags": ["String"],
  "language": "String",
  "thumbnail_url": "String",
  "status": "Enum('draft', 'published', 'archived')",
  "publishedAt": "Timestamp",
  "enrollment_count": "Integer", // Number of students enrolled
  "price": {
    "amount": "Decimal",
    "currency": "String"
  },
  "rating": {
    "average": "Decimal",
    "count": "Integer"
  },
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}

lessons
{
  "id": "UUID",
  "course_id": "UUID (Reference to courses.id)",
  "title": "String",
  "contentType": "Enum('text', 'video', 'quiz', pdf', 'docx')",
  "content_url": [
    {
      "type": "String",  // e.g., 'video', 'pdf', 'docx'
      "url": "String"    // URL to the content
    }
  ],
  "text_content": "String|null",     // For 'text' content type
  "quiz_id": "UUID|null", 
  "order": "Integer",  // Useful to define lesson order in course
  "status": "Enum('draft', 'published')",
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}

course_enrollments
{
  "id": "UUID",
  "user_id": "UUID (Reference to users.id)",
  "course_id": "UUID (Reference to courses.id)",
  "enrollment_date": "Timestamp",
  "progress": {
    "completed_lessons": ["UUID"], // List of lesson IDs completed
    "total_lessons": "Integer",
    "progress_percentage": "Decimal" // e.g., 75.5 for 75.5%
  },
  "status": "Enum('completed', 'in_progress')",
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}

lesson_progress
{
  "id": "UUID",
  "user_id": "UUID (Reference to users.id)",
  "lesson_id": "UUID (Reference to lessons.id)",
  "course_id": "UUID (Reference to courses.id)",
  "status": "Enum('not_started', 'in_progress', 'completed')",
  "has_assessment": "Boolean", // Indicates if the lesson has an assessment
  "assessment": {
    "id": "UUID|null", // Reference to the assessment if exists
    "score": "Decimal|null", // Score achieved in the assessment
    "passed": "Boolean|null", // Indicates if the assessment was passed
    "attempts": "Integer" // Number of attempts made
  },
  "is_completed": "Boolean",
  "started_at": "Timestamp|null",
  "completed_at": "Timestamp|null",
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}
```

## Quiz & Assessment Service
- Quizzes, grading
- Database: `PostgreSQL`

### Database Schema
```json
quizzes (MCQ type)
{
  "id": "UUID",
  "lesson_id": "UUID (Reference to lessons.id)",
  "title": "String",
  "description": "String",
  "passing_score": "Decimal",
  "question_count": "Integer",
  "time_limit": "INTEGER", // in minutes
  "attempt_limit": "INTEGER",
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}

quiz_questions
{
  "id": "UUID",                        // Unique identifier for the question
  "quiz_id": "UUID",                   // Reference to the quiz
  "type": "Enum('MULTIPLE_CHOICE', 'TRUE_FALSE', 'FILL_BLANK', 'MATCHING', ...)", // Question type
  "question_text": "String",           // The question prompt
  "options": [                        // List of options for multiple-choice questions
    {
      "option": "String",
      "is_correct": "Boolean"
    }
  ],
  "marks": "Integer",                   // Marks awarded for correct answer
  "media": {                            // Optional media attachments
    "image_url": "String|null",
    "video_url": "String|null"
  },
  "difficulty": "Enum('EASY', 'MEDIUM', 'HARD')", // Difficulty level
  "order": "Integer"                    // Display order within the quiz
}

assignments
{
  "id": "UUID",                        // Unique identifier for the assignment
  "course_id": "UUID",                 // Reference to the course
  "module_id": "UUID",                 // Reference to the module
  "instructor_id": "String",           // Instructor ID
  "title": "String",                   // Title of the assignment
  "instructions": "String",            // Submission instructions
  "due_date": "Timestamp",               // Due date for submission
  "max_marks": "Integer",                 // Maximum marks achievable
  "allow_late_submission": "Boolean",     // Whether late submissions are permitted
  "created_at": "Timestamp"               // Creation timestamp
}

quiz_attempts
{
  "id": "UUID",
  "user_id": "UUID (Reference to users.id)",
  "quiz_or_assignment_id": "UUID", 
  "type": "Enum('QUIZ', 'ASSIGNMENT')",  // Type of submission
  "attempt_number": "Integer", // e.g., 1 for first attempt
  "score": "Decimal",
  "status": "Enum('SUBMITTED', 'GRADED', 'LATE', 'PENDING')", // Submission status
  "passed": "Boolean",
  "answers": [                          // List of answers (for quizzes)
    {
      "question_id": "UUID",
      "selected_options": ["String"],    // Selected options
      "is_correct": "Boolean"             // Correctness flag
    }
  ],
  "text_answer": "String|null",           // For written answers in assignments
  "file_url": "String|null",               // For uploaded files in assignments
  "started_at": "Timestamp",
  "completed_at": "Timestamp",
  "time_taken": "Integer", // Time taken in seconds
  "created_at": "Timestamp",
}
  
user_answers
{
  "id": "UUID",
  "attempt_id": "UUID (Reference to quiz_attempts.id)",
  "question_id": "UUID (Reference to quiz_questions.id)",
  "selected_option_id": "UUID|null", // Reference to quiz_options.id if applicable
  "is_correct": "Boolean", // Indicates if the answer was correct
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}


grading (optional)
{
  "id": "UUID",                        // Unique identifier for the grading record
  "submission_id": "UUID",             // Reference to the submission
  "graded_by": "String",                 // Instructor ID who graded
  "marks_obtained": "Integer",           // Marks obtained
  "feedback": "String",                  // Feedback provided
  "grading_date": "Timestamp",           // Date of grading
  "grade_scale": "String|null",          // Grade scale (e.g., A, B, C), optional
  "manual_grading": "Boolean"            // Indicates if grading was manual
}
```

## Payment Service
- Payment processing, transaction history
- Database: `PostgreSQL`

### Database Schema
```json
transactions
{
  "id": "UUID",                                    // Unique identifier for the transaction
  "user_id": "UUID (reference to users.id)",       // ID of the user who made the payment
  "subscription_id": "UUID|null",                   // Optional link to subscription
  "amount": "Decimal",                           // Payment amount in main currency units (e.g., 29.99), stored as Decimal128
  "currency": "String",                              // Currency code, e.g., 'USD'
  "status": "Enum('PENDING', 'FAILED', 'SUCCESS', 'REFUNDED')",
  "payment_method_id": "UUID",                       // Reference to the saved payment method
  "gateway": "Enum('stripe', 'paypal')",            // Payment gateway provider
  "gateway_transaction_id": "String",               // Transaction ID from the gateway
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}
invoice
{
  "id": "UUID",                                    // Unique identifier for the invoice
  "user_id": "UUID (reference to users.id)",       // ID of the user
  "transaction_id": "UUID",                          // Reference to associated transaction
  "invoice_number": "String",                        // Invoice number like 'INV-2025-0711-001'
  "items": [
    {
      "description": "String",                       // Item description
      "amount": "Decimal128",                        // Item amount (e.g., 29.99), stored as Decimal128
      "quantity": "Integer"                          // Quantity of the item
    }
  ],
  "tax_amount": "Decimal128",                        // Tax amount, stored as Decimal128
  "total_amount": "Decimal128",                      // Total invoice amount, stored as Decimal128
  "currency": "String",                              // Currency code
  "issue_date": "Timestamp",                         // Date of issue
  "due_date": "Timestamp",                           // Payment due date
  "status": "Enum('PAID', 'UNPAID', 'CANCELLED')"
}
refunds
{
  "id": "UUID",                                    // Unique identifier for the refund
  "transaction_id": "UUID",                        // Reference to the related transaction
  "user_id": "UUID (reference to users.id)",       // User's ID
  "refund_amount": "Decimal128",                    // Refund amount, stored as Decimal128
  "currency": "String",                              // Currency code
  "reason": "String",                                // Reason for the refund
  "refund_date": "Timestamp",                        // Date when refund was issued
  "gateway_refund_id": "String",                     // Gateway-specific refund ID
  "status": "Enum('PENDING', 'COMPLETED', 'FAILED')"
}

payment_methods (for saved payment methods)
{
  "id": "UUID",                                // Unique identifier for the payment method
  "user_id": "UUID (reference to users.id)",   // User's ID
  "provider": "Enum('stripe', 'paypal')",      // Payment provider
  "type": "Enum('credit_card', 'wallet', 'bank')", // Payment method type
  "card": {
    "last4": "String",                          // Last 4 digits
    "brand": "String",                          // Card brand, e.g., 'Visa'
    "expiry_month": "Integer",                  // Expiry month
    "expiry_year": "Integer"                    // Expiry year
  } | null,                                    // Nullable if not a card
  "provider_customer_id": "String",             // Customer ID/Token from provider
  "is_default": "Boolean",                      // Default payment method flag
  "is_active": "Boolean",                       // Active status for soft delete
  "created_at": "Timestamp",                    // Creation timestamp
  "updated_at": "Timestamp"                     // Last update timestamp
}

tax_records (optional)
{
  "id": "UUID",                                // Unique identifier for the tax record
  "country": "String",                         // Country code, e.g., 'US'
  "state": "String|null",                       // State code, e.g., 'CA', nullable if not applicable
  "tax_rate": "Decimal128",                     // Tax rate, e.g., 0.08, stored as Decimal128
  "description": "String",                      // Description of the tax (e.g., 'California state tax')
  "tax_type": "Enum('VAT', 'GST', 'SALES_TAX')", // Tax type
  "effective_from": "Timestamp",                // Effective start date
  "effective_to": "Timestamp|null"             // Effective end date or null if ongoing
}

instructor_payouts
{
  "id": "UUID",                                // Unique identifier for the payout record
  "instructor_id": "UUID",                     // Instructor's ID
  "amount": "Decimal128",                       // Amount earned, stored as Decimal128
  "currency": "String",                         // Currency code, e.g., 'USD'
  "status": "Enum('PENDING', 'COMPLETED', 'FAILED')",
  "method": "Enum('BANK_TRANSFER', 'PAYPAL', 'STRIPE_CONNECT')",
  "related_courses": ["String"],                // List of course IDs related to this payout
  "period_start": "Timestamp",                  // Earnings period start date
  "period_end": "Timestamp",                    // Earnings period end date
  "scheduled_at": "Timestamp",                  // Scheduled payout date
  "paid_at": "Timestamp|null",                   // Actual payout date or null if pending
  "note": "String"                              // Additional notes
}
```

## Subscription  Service
-Managing plans, user subscriptions, cancellations, renewals, and access control

plans
```json
plans
{
  "id": "UUID",                    // Unique identifier for the plan
  "name": "String",                // Plan name, e.g., 'Pro Monthly'
  "plan_code": "String",           // Internal code or external sync identifier, e.g., 'PRO_MONTHLY'
  "price": "Decimal128",           // Price amount, stored as Decimal128 (e.g., 19.99)
  "currency": "String",            // Currency code, e.g., 'USD'
  "billing_cycle": "Enum('MONTHLY', 'ANNUAL', 'LIFETIME')",
  "features": ["String"],          // List of feature descriptions
  "trial_days": "Integer",         // Number of trial days
  "is_active": "Boolean",          // Whether the plan is active
  "created_at": "Timestamp",       // Creation timestamp
  "updated_at": "Timestamp"        // Last update timestamp
}

subscriptions
{
  "id": "UUID",                                // Unique identifier for the user subscription
  "user_id": "UUID",                           // User's ID
  "plan_id": "UUID",                           // Reference to the subscription plan
  "payment_transaction_id": "UUID",            // ID of the first payment transaction
  "status": "Enum('ACTIVE', 'CANCELLED', 'EXPIRED', 'TRIAL')",
  "start_date": "Timestamp",                   // Subscription start date
  "end_date": "Timestamp|null",                 // End date, null if ongoing
  "next_billing_date": "Timestamp",            // Next billing date
  "is_auto_renew": "Boolean",                   // Auto-renewal toggle
  "grace_period_until": "Timestamp|null",      // Grace period end date if payment failed
  "created_at": "Timestamp"                     // Record creation timestamp
}

cancellations
{
  "id": "UUID",                        // Unique identifier for the cancellation record
  "subscription_id": "UUID",           // Reference to the user subscription
  "user_id": "UUID",                     // User's ID
  "cancelled_at": "Timestamp",           // Cancellation date
  "cancel_reason": "String",             // Reason for cancellation
  "feedback": "String",                  // Optional user feedback
  "was_mid_cycle": "Boolean"             // Whether cancellation occurred mid-cycle
}

renewls
{
  "id": "UUID",                        // Unique identifier for the payment attempt
  "subscription_id": "UUID",           // Reference to the user subscription
  "user_id": "UUID",                     // User's ID
  "attempted_at": "Timestamp",           // When the attempt was made
  "status": "Enum('SUCCESS', 'FAILED', 'RETRIED')", // Payment status
  "amount": "Decimal128",                // Payment amount (e.g., in cents as Decimal128)
  "transaction_id": "UUID",              // Related transaction ID
  "retry_count": "Integer",              // Number of retries attempted
  "failure_reason": "String|null"        // Optional failure reason if failed
}

grace_period
{
  "id": "UUID",                        // Unique identifier for the access record
  "user_id": "UUID",                     // User's ID
  "subscription_id": "UUID",             // Reference to the subscription
  "granted_at": "Timestamp",             // Access granted date
  "expires_at": "Timestamp",             // Expiry date of access
  "reason": "String",                    // Reason for hold or temporary access
  "status": "Enum('ACTIVE', 'EXPIRED')" // Current status
}





```


## Certificate Service (stage 2)
- Certificate generation, verification
- SaaS for certificate templates: Certifier (https://www.certifier.io/)
- Database: `PostgreSQL`

### Database Schema
```json
certificate_templates
{
  "id": "UUID",
  "name": "String",
  "description": "String",
  "design_url": "String", // URL to the certificate template or design
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}

certificates
{
  "id": "UUID",
  "user_id": "UUID (Reference to users.id)",
  "course_id": "UUID (Reference to courses.id)",
  "template_id": "UUID (Reference to certificate_templates.id)",
  "issue_date": "Timestamp",
  "expiry_date": "Timestamp|null", // Optional expiration
  "certificate_url": "String", // URL to the generated certificate file
  "verification_code": "String", // Public code for verification (e.g., CERT-AB12-XY34)
  "file_type": "Enum('pdf', 'png')",
  "status": "Enum('issued', 'revoked')",
  "generated_at": "Timestamp",
  "metadata": {
    // JSON object for additional info like grade, score, etc.
    "grade": "String",
    "score": "Number",
    "completion_time": "String"
  },
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}

certificate_verifications
{
  "id": "UUID",
  "certificate_id": "UUID (Reference to certificates.id)",
  "verification_code": "String", // Duplicate for indexing
  "verified": "Boolean",
  "verified_at": "Timestamp|null",
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}
```

## Notification Service (stage 2)
- Email notifications, alerts, reminders
- Database: `PostgreSQL` + Message Queue (RabbitMQ/Kafka)

### Database Schema
```json
email_templates
{
  "id": "UUID",                        // Unique identifier for the template
  "name": "String",                    // Template name, e.g., 'Course Enrollment Confirmation'
  "template_code": "String",           // Internal/template code, e.g., 'COURSE_ENROLL_CONFIRM'
  "subject": "String",                   // Email subject with placeholders
  "body_html": "String",                 // HTML body with placeholders
  "body_text": "String",                 // Text body with placeholders
  "placeholders": ["String"],            // List of placeholders used in the template
  "language": "String",                  // Language code, e.g., 'en'
  "created_by": "UUID",                  // ID of creator or admin
  "created_at": "Timestamp",             // Creation timestamp
  "updated_at": "Timestamp",             // Last update timestamp
  "is_active": "Boolean"                   // Whether the template is active
}

scheduled_notifications
{
  "id": "UUID",                        // Unique identifier for the message record
  "user_id": "UUID",                     // User's ID
  "template_code": "String",             // Template code, e.g., 'COURSE_ENROLL_CONFIRM'
  "channel": "Enum('EMAIL', 'PUSH', 'SMS')", // Channel for delivery
  "send_at": "Timestamp",                // Scheduled send time
  "status": "Enum('PENDING', 'SENT', 'FAILED', 'CANCELLED')", // Sending status
  "retry_count": "Integer",              // Number of retries
  "payload": {"String": "Mixed"},        // Data to fill placeholders
  "target_contact": "String",            // Email, phone number, or device token
  "created_at": "Timestamp",             // Record creation time
  "last_attempt_at": "Timestamp|null"    // Last attempt time, null if not attempted yet
}

notification_logs
{
  "id": "UUID",                        // Unique identifier for the delivery record
  "notification_id": "UUID",           // Reference to scheduled_notifications
  "user_id": "UUID",                     // User's ID
  "channel": "Enum('EMAIL', 'PUSH', 'SMS')", // Delivery channel
  "status": "Enum('SENT', 'FAILED', 'SKIPPED')", // Delivery status
  "sent_at": "Timestamp",                // When the message was sent
  "response": {                          // Provider's response details
    "message_id": "String",              // ID from email provider
    "provider": "String"                 // Provider name, e.g., 'SendGrid'
  }|null,                              // Nullable if no response
  "error": "String" | null,               // Error message if any
  "retry_count": "Integer"               // Retry attempt count
}


preferences
{
  "id": "UUID",                        // Unique identifier for the preferences record
  "user_id": "UUID",                     // User's ID
  "channels": {                          // Notification channels enabled
    "EMAIL": "Boolean",
    "SMS": "Boolean",
    "PUSH": "Boolean"
  },
  "notification_types": {                // Types of notifications enabled
    "course_enrollments": "Boolean",
    "certificates": "Boolean",
    "marketing": "Boolean",
    "reminders": "Boolean"
  },
  "digest_frequency": "Enum('IMMEDIATE', 'DAILY', 'WEEKLY')", // Frequency of digest
  "do_not_disturb": {                    // DND settings
    "enabled": "Boolean",
    "start_time": "String",              // Format 'HH:MM'
    "end_time": "String"                 // Format 'HH:MM'
  },
  "updated_at": "Timestamp"              // Last update timestamp
}

```

## Analytics Service (stage 2)
- Reporting, dashboards, metrics
- Database: `PostgreSQL`

### Database Schema
```json
user_analytics
{
  "id": "UUID",
  "user_id": "UUID (Reference to users.id)",
  "total_courses_enrolled": "Integer",
  "total_lessons_completed": "Integer",
  "total_quizzes_attempted": "Integer",
  "average_assessment_score": "Decimal",
  "last_active": "Timestamp",
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}

course_analytics
{
  "id": "UUID",
  "course_id": "UUID (Reference to courses.id)",
  "total_enrollments": "Integer",
  "average_rating": "Decimal",
  "total_lessons": "Integer",
  "total_quizzes": "Integer",
  "total_students_completed": "Integer",
  "average_completion_rate": "Decimal", // e.g., 75.5 for 75.5%
  "average_student_score": "Decimal",
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}
```

## Ai Service (stage 2)
- AI-driven recommendations, chatbots
- Database: `MongoDB` + Message Queue (RabbitMQ/Kafka)

### Database Schema
```json
user_preferences
{
  "id": "UUID",
  "user_id": "UUID (Reference to users.id)",
  "preferences": {
    "instructors": ["UUID"], // List of instructor IDs
    "course_categories": ["String"], // e.g., ['programming', 'design']
    "subjects": ["String"], // e.g., ['Python', 'JavaScript']
  },
  "last_updated": "Timestamp",
  "created_at": "Timestamp"
}

user_recommendations
{
  "id": "UUID",
  "user_id": "UUID (Reference to users.id)",
  "recommended_courses": ["UUID"], // List of course IDs
  "recommended_instructors": ["UUID"], // List of instructor IDs
  "generated_at": "Timestamp",
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}

ai_chat_sessions
{
  "id": "UUID",
  "user_id": "UUID (Reference to users.id)",
  "session_id": "String", // Unique session identifier
  "messages": [
    {
      "role": "Enum('user', 'assistant')",
      "content": "String",
      "timestamp": "Timestamp"
    }
  ],
  "created_at": "Timestamp",
  "updated_at": "Timestamp"
}
```
