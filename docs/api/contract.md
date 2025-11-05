openapi: 3.0.3
info:
  title: Event Management Platform API
  description: |
    Enterprise-grade API contract for the Global Events Platform (GEP) - a comprehensive event management system.
    
    ## Microservices Architecture
    - **Auth Service**: Authentication, authorization, and user management
    - **Event Service**: Event creation, management, and publishing
    - **Notification Service**: Email and notification delivery
    - **Payment Service**: Payment gateway integration (future)
    
    ## Security
    All endpoints require JWT Bearer token authentication unless explicitly marked as public.
    
  version: 1.0.0
  contact:
    name: GEP API Support
    email: api-support@gep.com

servers:
  - url: https://api.sankofagrid.com/
    description: Production
  - url: https://api-staging.gep.com/v1
    description: Staging
  - url: http://localhost:8000/
    description: Local Development

tags:
  - name: Authentication
    description: User authentication and authorization operations
  - name: User Management
    description: User CRUD operations and account management
  - name: Event Management
    description: Event creation, updates, and lifecycle management
  - name: Registration
    description: Event registration and attendee management
  - name: Venue Management
    description: Venue and layout configuration
  - name: Ticket Management
    description: Ticket tiers, pricing, and allocation
  - name: Discount Management
    description: Discount rules and conditional pricing
  - name: Analytics
    description: Event and platform analytics
  - name: Invitations
    description: Co-organizer and attendee invitations
  - name: Notifications
    description: Email and notification management

security:
  - BearerAuth: []

paths:
  # ============================================
  # AUTHENTICATION ENDPOINTS ✅
  # ============================================
  ```yaml
    endpoints:
      - name: Login
        description: Authenticates a user and sends an OTP for verification.
        path: /api/v1/auth/auth/login
        method: POST
        request:
          headers:
            Content-Type: application/json
          body:
            type: object
            required:
              - email
              - password
            properties:
              email:
                type: string
                format: email
                example: user@example.com
              password:
                type: string
                format: password
                example: P@ssword123
        responses:
          200:
            description: "OTP sent to user's email".
            
          401:
            description: Invalid credentials.
          
          403:
            description: "User account is inactive"
      - name: Register
        description: Registers a new user account.
        path: /api/v1/auth/register
        method: POST
        request:
          headers:
            Content-Type: application/json
          body:
            type: object
            required:
              - fullName
              - email
              - password
              - confirmPassword
            properties:
              fullName:
                type: string
                example: John Doe
              email:
                type: string
                format: email
                example: john@example.com
              password:
                type: string
                format: password
                minLength: 8
              confirmPassword:
                type: string
                format: password
        responses:
          201:
            description: User successfully registered.
            body:
              type: object
              properties:
                id:
                  type: long
                  example: 200
                fullName:
                  type: string
                  example: John Doe
          400:
            description: Invalid registration details.
          409:
            description: Email already in use.
          500:
            description: Unexpected Error Occurred
          
    
      - name: Verify OTP
        description: Verifies the OTP sent to the user’s email after registration.
        path: /api/v1/auth/verify-otp
        method: POST
        request:
          headers:
            Content-Type: application/json
          body:
            type: object
            required:
              - otp
              - email
            properties:
              otp:
                type: string
                example: "123456"
              email:
                type: string
                example: 123@example.com
        responses:
          200:
            description: OTP verification successful.
            body:
              type: object
              properties:
                id:
                  type: long
                  description: User's id
                email:
                  type: string
                  description: User's email
                role:
                  type: string
                  description: User's role
          409:
            description: Invalid or expired OTP.
          401:
            description: User account not found
          403:
            description: User account not active
    
      - name: Forgot Password
        description: Sends a password reset link or code to the user's email.
        path: /api/v1/auth/forgot-password
        method: POST
        request:
          headers:
            Content-Type: application/json
          body:
            type: object
            required:
              - email
            properties:
              email:
                type: string
                format: email
                example: user@example.com
        responses:
          200:
            description: Password reset email sent.
          401:
            description: User account not found
          403:
            description: User account not active
            
      - name: Reset Password
        description: Resets the user's password after verifying otp.
        path: /api/v1/auth/reset-password
        method: POST
        request:
          headers:
            Content-Type: application/json
          body:
            type: object
            required:
              - otp
              - email
              - password
            properties:
              otp:
                type: string
                example: "123456"
              email:
                type: string
                format: email
                example: user@example.com
              password:
                type: string
                format: password
                minLength: 8
        responses:
          200:
            description: Password has been reset successfully
          401:
            description: User account not found
          403:
            description: User account not active
          409:
            description: Invalid or expired OTP.
            
      - name: Refresh Token
        description: Refreshes the access token.
        path: /api/v1/auth/refresh-token
        method: POST
        request:
          headers:
            Content-Type: application/json
          body:
            type: object
            required:
              - refreshToken
            properties:
              refresh_token:
                type: string
          responses:
            200:
              description: Access token refreshed successfully.
              body:
                type: object
                properties:
                  access_token:
                    type: string
                    description: JWT access token.
            400:
              description: Invalid refresh token.

  ```

  # ============================================
  # USER MANAGEMENT ENDPOINTS ✅
  # ============================================
  ```yaml
    user-management-endpoints:
    - name: Get User
      description: Retrieves the user details of a specified user.
      path: /users/{userId}
      method: GET
      request:
        headers:
          Content-Type: application/json
          authorization: Bearer {access_token}
      responses:
        200:
          data:
            type: object
            properties:
              userId:
                type: string
              fullName:
                type: string
              email:
                type: string
              phone:
                type: string
              address:
                type: string
              profileImageUrl:
                type: string
              status:
                type: boolean
        404:
          message: User not found.

    - name: update User
      description: Updates the user details of a specified user.
      path: /users/{userId}
      method: PUT
      request:
        headers:
          Content-Type: application/json
          authorization: Bearer {access_token}
        body:
          type: object
          properties:
            fullName:
              type: string
            email:
              type: string
            phone:
              type: string
            address:
              type: string
            status:
              type: boolean
      responses:
        200:
          data:
            type: object
            properties:
              userId:
                type: Long
              fullName:
                type: string
              email:
                type: string
              phone:
                type: string
              address:
                type: string
              profileImageUrl:
                type: string
              status:
        400:
          message: Invalid request.
        404:
          message: User not found.

    - name: deactivate User/ reactivate User
      description: Deactivates/reactivates the user account of a specified user.
      path: /users/{userId}/deactivate
      method: POST
      request:
        headers:
          Content-Type: application/json
          authorization: Bearer {access_token}
      responses:
        200:

    - name: search User - Admin Dashboard
      description: Searches for a user by name, email and filters by status and role.
      path: /users/search
      method: GET
      request:
        headers:
          Content-Type: application/json
          authorization: Bearer {access_token}
        parameters:
          - name: keyword
            in: query
            description: Name of the user to search for.
            required: false
            type: string
          - name: role
            in: query
            description: Role of the user to filter.
            required: false
            type: string
          - name: status
            in: query
            description: Status of the user to filter.
            required: false
            type: boolean
          - name: page
            in: query
            description: Page number.
            required: false
            type: integer
      responses:
        200:
          data:
            type: object
            properties:
              users:
                type: array
                items:
                  type: object
                  properties:
                    userId:
                      type: Long
                    fullName:
                      type: string
                    email:
                      type: string
                    role:
                      type: string
                    status:
                      type: boolean
                    profileImageUrl:
                      type: string
                    eventsOrganized:
                      type: Long
                    eventsAttended:
                      type: Long

    - name: user management - Admin Dashboard
      description: Overview and list of all users.
      path: /users/management
      method: GET
      request:
        headers:
          Content-Type: application/json
          authorization: Bearer {access_token}
      responses:
        200:
          data:
            type: object
            properties:
              totalUsers:
                type: Long
              totalOrganizers:
                type: Long
              totalAttendees:
                type: Long
              totalDeactivatedUsers:
                type: Long
              users:
                type: array
                items:
                  type: object
                  properties:
                    totalElements:
                      type: Long
                    totalPages:
                      type: Long
                    size:
                      type: Long
                    number:
                      type: Long
                    content:
                      type: array
                      items:
                        type: object
                        properties:
                          userId:
                            type: Long
                          fullName:
                            type: string
                          email:
                            type: string
                          role:
                            type: string
                          status:
                            type: boolean
                          profileImageUrl:
                            type: string
                          eventsOrganized:
                            type: Long
                          eventsAttended:
                            type: Long
  ```
  # ============================================
  # EVENT MANAGEMENT ENDPOINTS
  # ============================================
  /events:
    get:
      tags:
        - Event Management
      summary: Get all events
      description: Retrieves paginated list of events with filters
      operationId: getAllEvents
      security: []
      parameters:
        - $ref: '#/components/parameters/PageNumber'
        - $ref: '#/components/parameters/PageSize'
        - name: category
          in: query
          schema:
            type: string
        - name: location
          in: query
          description: City or region
          schema:
            type: string
        - name: startDate
          in: query
          schema:
            type: string
            format: date
        - name: endDate
          in: query
          schema:
            type: string
            format: date
        - name: eventType
          in: query
          schema:
            type: string
            enum: [IN_PERSON, VIRTUAL, HYBRID]
        - name: status
          in: query
          schema:
            type: string
            enum: [DRAFT, PENDING_APPROVAL, PUBLISHED, CANCELLED, COMPLETED]
        - name: search
          in: query
          description: Search in title and description
          schema:
            type: string
      responses:
        '200':
          description: Events retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  data:
                    type: array
                    items:
                      $ref: '#/components/schemas/EventSummary'
                  pagination:
                    $ref: '#/components/schemas/PaginationMetadata'

    post:
      tags:
        - Event Management
      summary: Create new event
      description: Creates a new event (Organizer role required)
      operationId: createEvent
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateEventRequest'
      responses:
        '201':
          description: Event created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/EventResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '403':
          $ref: '#/components/responses/Forbidden'

  /events/{eventId}:
    get:
      tags:
        - Event Management
      summary: Get event by ID
      description: Retrieves detailed event information
      operationId: getEventById
      security: []
      parameters:
        - $ref: '#/components/parameters/EventId'
      responses:
        '200':
          description: Event retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/EventDetailResponse'
        '404':
          $ref: '#/components/responses/NotFound'

    put:
      tags:
        - Event Management
      summary: Update event
      description: Updates event details (Organizer only)
      operationId: updateEvent
      parameters:
        - $ref: '#/components/parameters/EventId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateEventRequest'
      responses:
        '200':
          description: Event updated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/EventResponse'
        '400':
          $ref: '#/components/responses/BadRequest'
        '403':
          $ref: '#/components/responses/Forbidden'
        '404':
          $ref: '#/components/responses/NotFound'

    delete:
      tags:
        - Event Management
      summary: Delete event
      description: Soft deletes an event
      operationId: deleteEvent
      parameters:
        - $ref: '#/components/parameters/EventId'
      responses:
        '204':
          description: Event deleted successfully
        '403':
          $ref: '#/components/responses/Forbidden'
        '404':
          $ref: '#/components/responses/NotFound'

  /events/{eventId}/publish:
    post:
      tags:
        - Event Management
      summary: Publish event
      description: Submits event for approval or publishes directly based on settings
      operationId: publishEvent
      parameters:
        - $ref: '#/components/parameters/EventId'
      responses:
        '200':
          description: Event published/submitted successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/EventResponse'
        '400':
          description: Event incomplete or invalid
        '404':
          $ref: '#/components/responses/NotFound'

  /events/{eventId}/cancel:
    post:
      tags:
        - Event Management
      summary: Cancel event
      description: Cancels a published event
      operationId: cancelEvent
      parameters:
        - $ref: '#/components/parameters/EventId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required:
                - reason
              properties:
                reason:
                  type: string
                notifyAttendees:
                  type: boolean
                  default: true
      responses:
        '200':
          description: Event cancelled successfully
        '404':
          $ref: '#/components/responses/NotFound'

  /events/{eventId}/approve:
    post:
      tags:
        - Event Management
      summary: Approve event (Admin only)
      description: Approves pending event for publication
      operationId: approveEvent
      parameters:
        - $ref: '#/components/parameters/EventId'
      responses:
        '200':
          description: Event approved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/EventResponse'
        '403':
          $ref: '#/components/responses/Forbidden'
        '404':
          $ref: '#/components/responses/NotFound'

  /events/{eventId}/reject:
    post:
      tags:
        - Event Management
      summary: Reject event (Admin only)
      description: Rejects pending event
      operationId: rejectEvent
      parameters:
        - $ref: '#/components/parameters/EventId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: object
              required:
                - reason
              properties:
                reason:
                  type: string
      responses:
        '200':
          description: Event rejected successfully
        '403':
          $ref: '#/components/responses/Forbidden'
        '404':
          $ref: '#/components/responses/NotFound'

  /events/{eventId}/banner:
    post:
      tags:
        - Event Management
      summary: Upload event banner
      description: Uploads banner image for event
      operationId: uploadEventBanner
      parameters:
        - $ref: '#/components/parameters/EventId'
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              required:
                - file
              properties:
                file:
                  type: string
                  format: binary
      responses:
        '200':
          description: Banner uploaded successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  bannerUrl:
                    type: string
                    format: uri
        '400':
          description: Invalid file format or size
        '404':
          $ref: '#/components/responses/NotFound'

    delete:
      tags:
        - Event Management
      summary: Delete event banner
      description: Removes event banner image
      operationId: deleteEventBanner
      parameters:
        - $ref: '#/components/parameters/EventId'
      responses:
        '204':
          description: Banner deleted successfully
        '404':
          $ref: '#/components/responses/NotFound'

  /events/organizer/{organizerId}:
    get:
      tags:
        - Event Management
      summary: Get events by organizer
      description: Retrieves all events for specific organizer
      operationId: getEventsByOrganizer
      parameters:
        - name: organizerId
          in: path
          required: true
          schema:
            type: string
            format: uuid
        - $ref: '#/components/parameters/PageNumber'
        - $ref: '#/components/parameters/PageSize'
      responses:
        '200':
          description: Events retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  data:
                    type: array
                    items:
                      $ref: '#/components/schemas/EventSummary'
                  pagination:
                    $ref: '#/components/schemas/PaginationMetadata'

  # ============================================
  # VENUE MANAGEMENT ENDPOINTS
  # ============================================
  /venues:
    get:
      tags:
        - Venue Management
      summary: Get all venues
      description: Retrieves list of venues with search
      operationId: getAllVenues
      parameters:
        - $ref: '#/components/parameters/PageNumber'
        - $ref: '#/components/parameters/PageSize'
        - name: search
          in: query
          description: Search by venue name or location
          schema:
            type: string
        - name: city
          in: query
          schema:
            type: string
      responses:
        '200':
          description: Venues retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  data:
                    type: array
                    items:
                      $ref: '#/components/schemas/VenueSummary'
                  pagination:
                    $ref: '#/components/schemas/PaginationMetadata'

    post:
      tags:
        - Venue Management
      summary: Create venue
      description: Creates a new venue
      operationId: createVenue
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateVenueRequest'
      responses:
        '201':
          description: Venue created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VenueResponse'

  /venues/{venueId}:
    get:
      tags:
        - Venue Management
      summary: Get venue by ID
      description: Retrieves detailed venue information
      operationId: getVenueById
      parameters:
        - $ref: '#/components/parameters/VenueId'
      responses:
        '200':
          description: Venue retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VenueDetailResponse'
        '404':
          $ref: '#/components/responses/NotFound'

    put:
      tags:
        - Venue Management
      summary: Update venue
      description: Updates venue information
      operationId: updateVenue
      parameters:
        - $ref: '#/components/parameters/VenueId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateVenueRequest'
      responses:
        '200':
          description: Venue updated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VenueResponse'

  /venues/{venueId}/layout:
    post:
      tags:
        - Venue Management
      summary: Upload venue layout
      description: Uploads venue floor plan (JPG, PNG, or DXF)
      operationId: uploadVenueLayout
      parameters:
        - $ref: '#/components/parameters/VenueId'
      requestBody:
        required: true
        content:
          multipart/form-data:
            schema:
              type: object
              required:
                - file
              properties:
                file:
                  type: string
                  format: binary
                fileType:
                  type: string
                  enum: [IMAGE, CAD]
      responses:
        '200':
          description: Layout uploaded successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  layoutId:
                    type: string
                    format: uuid
                  layoutUrl:
                    type: string
                    format: uri
                  fileType:
                    type: string

  /venues/{venueId}/sections:
    get:
      tags:
        - Venue Management
      summary: Get venue sections
      description: Retrieves all sections for a venue
      operationId: getVenueSections
      parameters:
        - $ref: '#/components/parameters/VenueId'
      responses:
        '200':
          description: Sections retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/VenueSectionResponse'

    post:
      tags:
        - Venue Management
      summary: Create venue section
      description: Defines a new section on venue layout
      operationId: createVenueSection
      parameters:
        - $ref: '#/components/parameters/VenueId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateVenueSectionRequest'
      responses:
        '201':
          description: Section created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VenueSectionResponse'

  /venues/{venueId}/sections/{sectionId}:
    put:
      tags:
        - Venue Management
      summary: Update venue section
      description: Updates section details and boundaries
      operationId: updateVenueSection
      parameters:
        - $ref: '#/components/parameters/VenueId'
        - $ref: '#/components/parameters/SectionId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateVenueSectionRequest'
      responses:
        '200':
          description: Section updated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/VenueSectionResponse'

    delete:
      tags:
        - Venue Management
      summary: Delete venue section
      description: Removes a venue section
      operationId: deleteVenueSection
      parameters:
        - $ref: '#/components/parameters/VenueId'
        - $ref: '#/components/parameters/SectionId'
      responses:
        '204':
          description: Section deleted successfully

  # ============================================
  # REGISTRATION ENDPOINTS
  # ============================================
  /events/{eventId}/register:
    post:
      tags:
        - Registration
      summary: Register for event
      description: Registers attendee for an event
      operationId: registerForEvent
      parameters:
        - $ref: '#/components/parameters/EventId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/EventRegistrationRequest'
      responses:
        '201':
          description: Registration successful
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RegistrationResponse'
        '400':
          description: Invalid registration data or event full
        '404':
          $ref: '#/components/responses/NotFound'

  /registrations/{registrationId}:
    get:
      tags:
        - Registration
      summary: Get registration details
      description: Retrieves registration information
      operationId: getRegistration
      parameters:
        - $ref: '#/components/parameters/RegistrationId'
      responses:
        '200':
          description: Registration retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RegistrationDetailResponse'
        '404':
          $ref: '#/components/responses/NotFound'

    delete:
      tags:
        - Registration
      summary: Cancel registration
      description: Cancels attendee registration
      operationId: cancelRegistration
      parameters:
        - $ref: '#/components/parameters/RegistrationId'
      responses:
        '204':
          description: Registration cancelled successfully
        '404':
          $ref: '#/components/responses/NotFound'

  /events/{eventId}/registrations:
    get:
      tags:
        - Registration
      summary: Get event registrations
      description: Retrieves all registrations for an event (Organizer only)
      operationId: getEventRegistrations
      parameters:
        - $ref: '#/components/parameters/EventId'
        - $ref: '#/components/parameters/PageNumber'
        - $ref: '#/components/parameters/PageSize'
        - name: status
          in: query
          schema:
            type: string
            enum: [CONFIRMED, PENDING, CANCELLED]
      responses:
        '200':
          description: Registrations retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  data:
                    type: array
                    items:
                      $ref: '#/components/schemas/RegistrationSummary'
                  pagination:
                    $ref: '#/components/schemas/PaginationMetadata'
                  summary:
                    type: object
                    properties:
                      totalRegistrations:
                        type: integer
                      confirmedCount:
                        type: integer
                      pendingCount:
                        type: integer
                      cancelledCount:
                        type: integer

  # ============================================
  # TICKET MANAGEMENT ENDPOINTS
  # ============================================
  /events/{eventId}/tickets:
    get:
      tags:
        - Ticket Management
      summary: Get event ticket tiers
      description: Retrieves all ticket tiers for an event
      operationId: getEventTickets
      parameters:
        - $ref: '#/components/parameters/EventId'
      responses:
        '200':
          description: Ticket tiers retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/TicketTierResponse'

    post:
      tags:
        - Ticket Management
      summary: Create ticket tier
      description: Creates a new ticket tier for event
      operationId: createTicketTier
      parameters:
        - $ref: '#/components/parameters/EventId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateTicketTierRequest'
      responses:
        '201':
          description: Ticket tier created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TicketTierResponse'

  /events/{eventId}/tickets/{tierId}:
    put:
      tags:
        - Ticket Management
      summary: Update ticket tier
      description: Updates ticket tier details
      operationId: updateTicketTier
      parameters:
        - $ref: '#/components/parameters/EventId'
        - $ref: '#/components/parameters/TierId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateTicketTierRequest'
      responses:
        '200':
          description: Ticket tier updated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TicketTierResponse'

    delete:
      tags:
        - Ticket Management
      summary: Delete ticket tier
      description: Removes a ticket tier
      operationId: deleteTicketTier
      parameters:
        - $ref: '#/components/parameters/EventId'
        - $ref: '#/components/parameters/TierId'
      responses:
        '204':
          description: Ticket tier deleted successfully
        '400':
          description: Cannot delete tier with existing sales

  /events/{eventId}/tickets/{tierId}/availability:
    get:
      tags:
        - Ticket Management
      summary: Check ticket availability
      description: Gets current availability for ticket tier
      operationId: getTicketAvailability
      parameters:
        - $ref: '#/components/parameters/EventId'
        - $ref: '#/components/parameters/TierId'
      responses:
        '200':
          description: Availability retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/TicketAvailabilityResponse'

  # ============================================
  # DISCOUNT MANAGEMENT ENDPOINTS
  # ============================================
  /events/{eventId}/discounts:
    get:
      tags:
        - Discount Management
      summary: Get event discounts
      description: Retrieves all discount rules for an event
      operationId: getEventDiscounts
      parameters:
        - $ref: '#/components/parameters/EventId'
      responses:
        '200':
          description: Discounts retrieved successfully
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/DiscountResponse'

    post:
      tags:
        - Discount Management
      summary: Create discount rule
      description: Creates a new discount with conditional rules
      operationId: createDiscount
      parameters:
        - $ref: '#/components/parameters/EventId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateDiscountRequest'
      responses:
        '201':
          description: Discount created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DiscountResponse'

  /events/{eventId}/discounts/{discountId}:
    get:
      tags:
        - Discount Management
      summary: Get discount details
      description: Retrieves detailed discount information
      operationId: getDiscountById
      parameters:
        - $ref: '#/components/parameters/EventId'
        - $ref: '#/components/parameters/DiscountId'
      responses:
        '200':
          description: Discount retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DiscountDetailResponse'

    put:
      tags:
        - Discount Management
      summary: Update discount
      description: Updates discount rules and conditions
      operationId: updateDiscount
      parameters:
        - $ref: '#/components/parameters/EventId'
        - $ref: '#/components/parameters/DiscountId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateDiscountRequest'
      responses:
        '200':
          description: Discount updated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DiscountResponse'

    delete:
      tags:
        - Discount Management
      summary: Delete discount
      description: Removes a discount rule
      operationId: deleteDiscount
      parameters:
        - $ref: '#/components/parameters/EventId'
        - $ref: '#/components/parameters/DiscountId'
      responses:
        '204':
          description: Discount deleted successfully

  /events/{eventId}/discounts/validate:
    post:
      tags:
        - Discount Management
      summary: Validate discount code
      description: Checks if discount code is valid for registration
      operationId: validateDiscount
      parameters:
        - $ref: '#/components/parameters/EventId'
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ValidateDiscountRequest'
      responses:
        '200':
          description: Discount validation result
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DiscountValidationResponse'

  # ============================================
  # INVITATION ENDPOINTS
  # ============================================
  ```yaml
    endpoints:
      - name: Send Invitation
        description: Allows admin or event organiser to send invite
        path: /api/v1/event-invitations
        method: POST
        request:
          headers:
            Content-Type: application/json
          body:
            type: object
            required:
              - invitationTitle
              - inviteeName
              - inviteeEmail
              - role
              - event
            properties:
              invitationTitle:
                type: string
                example: Hackathon 2024 Invitation
              inviteeName:
                type: string
                example: Benjamin Asare
              inviteeEmail:
                type: string
                format: email
                example: basare@example.com  
              role:
                type: string
                allowedValues: [ORGANISER, CO_ORGANIZER, VENUE_STAFF, ATTENDEE]
              event:
                type: long
                example: 100
              message:
                type: string
                
        responses:
          200:
            description: "Event invitation sent successfully".
            
          404:
            description: Event not found.
          409:
            description: "An invitation already exists for this email for this event"
          403:
            description: "User account is inactive"
          500:
            description: "Failed to publish invitation email to sqs"
            
      - name: Get Invitations
        description: Retrieves all invitations.
        path: /api/v1/event-invitations
        method: GET
        
        responses:
          200:
            description: Invitations List.
            body:
              type: object
              properties:
                id:
                  type: long
                  example: 200
                invitationTitle:
                  type: string
                  example: Hackathon 2025 invitation
                inviteeName:
                  type: string
                  example: Benjamin Asare
                invitationCode:
                  type: string
                  example: INV-123456
                    
          400:
            description: Invalid registration details.
          409:
            description: Email already in use.
          500:
            description: Unexpected Error Occurred
          
    
      - name: Resend Invitation
        description: Resend Invitation.
        path: /api/v1/event-invitations/{id}/resend
        method: PUT
        
        responses:
          200:
            description: Event invitation resent successfully.
            
          404:
            description: Invitation Not Found.
          409:
            description: "Cannot resend an already accepted invitation"
          403:
            description: "You are not authorized to resend this invitation"
          500:
            description: "Failed to publish invitation email to sqs"
    
      - name: Accept Invitation
        description: Sends a password reset link or code to the user's email.
        path: /api/v1/event-invitations/accept-invitation
        method: POST
        request:
          headers:
            Content-Type: application/json
          body:
            type: object
            required:
              - invitationCode
              - fullName
              - password
            properties:
              invitationCode:
                type: string
                example: INV-123456
        responses:
          200:
            description: Password reset email sent.
          401:
            description: User account not found
          403:
            description: User account not active
            
      - name: Reset Password
        description: Resets the user's password after verifying otp.
        path: /api/v1/auth/reset-password
        method: POST
        request:
          headers:
            Content-Type: application/json
          body:
            type: object
            required:
              - otp
              - email
              - password
            properties:
              otp:
                type: string
                example: "123456"
              email:
                type: string
                format: email
                example: user@example.com
              password:
                type: string
                format: password
                minLength: 8
        responses:
          200:
            description: Password has been reset successfully
          401:
            description: User account not found
          403:
            description: User account not active
          409:
            description: Invalid or expired OTP.
            
      - name: Refresh Token
        description: Refreshes the access token.
        path: /api/v1/auth/refresh-token
        method: POST
        request:
          headers:
            Content-Type: application/json
          body:
            type: object
            required:
              - refreshToken
            properties:
              refresh_token:
                type: string
          responses:
            200:
              description: Access token refreshed successfully.
              body:
                type: object
                properties:
                  access_token:
                    type: string
                    description: JWT access token.
            400:
              description: Invalid refresh token.

  ```
  # ============================================
  # ANALYTICS ENDPOINTS
  # ============================================
  /analytics/events/{eventId}:
    get:
      tags:
        - Analytics
      summary: Get event analytics
      description: Retrieves detailed analytics for specific event (Organizer only)
      operationId: getEventAnalytics
      parameters:
        - $ref: '#/components/parameters/EventId'
        - name: startDate
          in: query
          schema:
            type: string
            format: date
        - name: endDate
          in: query
          schema:
            type: string
            format: date
      responses:
        '200':
          description: Analytics retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/EventAnalyticsResponse'

  /analytics/events/{eventId}/sales:
    get:
      tags:
        - Analytics
      summary: Get event sales analytics
      description: Retrieves sales breakdown by tier
      operationId: getEventSalesAnalytics
      parameters:
        - $ref: '#/components/parameters/EventId'
      responses:
        '200':
          description: Sales analytics retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/SalesAnalyticsResponse'

  /analytics/events/{eventId}/discounts:
    get:
      tags:
        - Analytics
      summary: Get discount performance
      description: Retrieves discount usage analytics
      operationId: getDiscountAnalytics
      parameters:
        - $ref: '#/components/parameters/EventId'
      responses:
        '200':
          description: Discount analytics retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DiscountAnalyticsResponse'

  /analytics/platform:
    get:
      tags:
        - Analytics
      summary: Get platform-wide analytics (Admin only)
      description: Retrieves aggregated analytics across all events
      operationId: getPlatformAnalytics
      parameters:
        - name: startDate
          in: query
          schema:
            type: string
            format: date
        - name: endDate
          in: query
          schema:
            type: string
            format: date
        - name: groupBy
          in: query
          schema:
            type: string
            enum: [DAY, WEEK, MONTH, YEAR]
          default: MONTH
      responses:
        '200':
          description: Platform analytics retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PlatformAnalyticsResponse'
        '403':
          $ref: '#/components/responses/Forbidden'

  /analytics/organizers/{organizerId}:
    get:
      tags:
        - Analytics
      summary: Get organizer analytics
      description: Retrieves analytics for all events by organizer
      operationId: getOrganizerAnalytics
      parameters:
        - name: organizerId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Organizer analytics retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/OrganizerAnalyticsResponse'

  # ============================================
  # NOTIFICATION ENDPOINTS
  # ============================================
  /notifications:
    get:
      tags:
        - Notifications
      summary: Get user notifications
      description: Retrieves notifications for current user
      operationId: getNotifications
      parameters:
        - $ref: '#/components/parameters/PageNumber'
        - $ref: '#/components/parameters/PageSize'
        - name: read
          in: query
          schema:
            type: boolean
        - name: type
          in: query
          schema:
            type: string
            enum: [EVENT_APPROVED, EVENT_REJECTED, REGISTRATION_CONFIRMED, INVITATION_RECEIVED, EVENT_CANCELLED, EVENT_REMINDER]
      responses:
        '200':
          description: Notifications retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  data:
                    type: array
                    items:
                      $ref: '#/components/schemas/NotificationResponse'
                  pagination:
                    $ref: '#/components/schemas/PaginationMetadata'
                  unreadCount:
                    type: integer

  /notifications/{notificationId}/read:
    patch:
      tags:
        - Notifications
      summary: Mark notification as read
      description: Marks a notification as read
      operationId: markNotificationRead
      parameters:
        - $ref: '#/components/parameters/NotificationId'
      responses:
        '200':
          description: Notification marked as read

  /notifications/read-all:
    patch:
      tags:
        - Notifications
      summary: Mark all notifications as read
      description: Marks all user notifications as read
      operationId: markAllNotificationsRead
      responses:
        '200':
          description: All notifications marked as read

  /notifications/settings:
    get:
      tags:
        - Notifications
      summary: Get notification preferences
      description: Retrieves user's notification preferences
      operationId: getNotificationSettings
      responses:
        '200':
          description: Settings retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/NotificationSettingsResponse'

    put:
      tags:
        - Notifications
      summary: Update notification preferences
      description: Updates user's notification preferences
      operationId: updateNotificationSettings
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpdateNotificationSettingsRequest'
      responses:
        '200':
          description: Settings updated successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/NotificationSettingsResponse'

  # ============================================
  # AUDIT LOG ENDPOINTS
  # ============================================
  /audit/logs:
    get:
      tags:
        - Audit
      summary: Get audit logs (Admin only)
      description: Retrieves system audit logs
      operationId: getAuditLogs
      parameters:
        - $ref: '#/components/parameters/PageNumber'
        - $ref: '#/components/parameters/PageSize'
        - name: userId
          in: query
          schema:
            type: string
            format: uuid
        - name: action
          in: query
          schema:
            type: string
            enum: [LOGIN, LOGOUT, CREATE, UPDATE, DELETE, APPROVE, REJECT]
        - name: resource
          in: query
          schema:
            type: string
            enum: [USER, EVENT, REGISTRATION, VENUE, TICKET, DISCOUNT]
        - name: startDate
          in: query
          schema:
            type: string
            format: date-time
        - name: endDate
          in: query
          schema:
            type: string
            format: date-time
      responses:
        '200':
          description: Audit logs retrieved successfully
          content:
            application/json:
              schema:
                type: object
                properties:
                  data:
                    type: array
                    items:
                      $ref: '#/components/schemas/AuditLogResponse'
                  pagination:
                    $ref: '#/components/schemas/PaginationMetadata'
        '403':
          $ref: '#/components/responses/Forbidden'

# ============================================
# COMPONENTS
# ============================================
components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
      description: JWT token obtained from login endpoint

  parameters:
    UserId:
      name: userId
      in: path
      required: true
      schema:
        type: string
        format: uuid
      description: Unique user identifier

    EventId:
      name: eventId
      in: path
      required: true
      schema:
        type: string
        format: uuid
      description: Unique event identifier

    VenueId:
      name: venueId
      in: path
      required: true
      schema:
        type: string
        format: uuid
      description: Unique venue identifier

    SectionId:
      name: sectionId
      in: path
      required: true
      schema:
        type: string
        format: uuid
      description: Unique section identifier

    RegistrationId:
      name: registrationId
      in: path
      required: true
      schema:
        type: string
        format: uuid
      description: Unique registration identifier

    TierId:
      name: tierId
      in: path
      required: true
      schema:
        type: string
        format: uuid
      description: Unique ticket tier identifier

    DiscountId:
      name: discountId
      in: path
      required: true
      schema:
        type: string
        format: uuid
      description: Unique discount identifier

    InvitationId:
      name: invitationId
      in: path
      required: true
      schema:
        type: string
        format: uuid
      description: Unique invitation identifier

    NotificationId:
      name: notificationId
      in: path
      required: true
      schema:
        type: string
        format: uuid
      description: Unique notification identifier

    PageNumber:
      name: page
      in: query
      schema:
        type: integer
        minimum: 0
        default: 0
      description: Page number (0-indexed)

    PageSize:
      name: size
      in: query
      schema:
        type: integer
        minimum: 1
        maximum: 100
        default: 20
      description: Number of items per page

    SortBy:
      name: sortBy
      in: query
      schema:
        type: string
        default: createdAt
      description: Field to sort by

    SortOrder:
      name: sortOrder
      in: query
      schema:
        type: string
        enum: [ASC, DESC]
        default: DESC
      description: Sort order

  responses:
    BadRequest:
      description: Invalid request parameters
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'

    Unauthorized:
      description: Authentication required
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'

    Forbidden:
      description: Insufficient permissions
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'

    NotFound:
      description: Resource not found
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'

    Conflict:
      description: Resource conflict
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'

    InternalServerError:
      description: Internal server error
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/ErrorResponse'

  schemas:
    # ============================================
    # AUTHENTICATION SCHEMAS
    # ============================================
    UserRegistrationRequest:
      type: object
      required:
        - email
        - password
        - firstName
        - lastName
        - role
      properties:
        email:
          type: string
          format: email
          example: john.doe@example.com
        password:
          type: string
          format: password
          minLength: 8
          pattern: '^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]'
          description: Must contain uppercase, lowercase, number, and special character
        firstName:
          type: string
          minLength: 1
          maxLength: 50
        lastName:
          type: string
          minLength: 1
          maxLength: 50
        phoneNumber:
          type: string
          pattern: '^\+?[1-9]\d{1,14}
        organization:
          type: string
          maxLength: 100
        role:
          type: string
          enum: [ORGANIZER, ATTENDEE]
          description: User role (ADMIN can only be created by existing admin)

    UserRegistrationResponse:
      type: object
      properties:
        userId:
          type: string
          format: uuid
        email:
          type: string
          format: email
        message:
          type: string
          example: "Registration successful. Please check your email to verify your account."

    LoginRequest:
      type: object
      required:
        - email
        - password
      properties:
        email:
          type: string
          format: email
        password:
          type: string
          format: password
        rememberMe:
          type: boolean
          default: false

    LoginResponse:
      type: object
      properties:
        accessToken:
          type: string
          description: JWT access token
        refreshToken:
          type: string
          description: JWT refresh token
        tokenType:
          type: string
          example: Bearer
        expiresIn:
          type: integer
          description: Token expiration time in seconds
          example: 3600
        user:
          $ref: '#/components/schemas/UserResponse'

    TokenResponse:
      type: object
      properties:
        accessToken:
          type: string
        refreshToken:
          type: string
        tokenType:
          type: string
          example: Bearer
        expiresIn:
          type: integer

    # ============================================
    # USER MANAGEMENT SCHEMAS
    # ============================================
    CreateUserRequest:
      type: object
      required:
        - email
        - firstName
        - lastName
        - role
      properties:
        email:
          type: string
          format: email
        firstName:
          type: string
        lastName:
          type: string
        phoneNumber:
          type: string
        organization:
          type: string
        role:
          type: string
          enum: [ADMIN, ORGANIZER, ATTENDEE]
        sendInvitationEmail:
          type: boolean
          default: true

    UpdateUserRequest:
      type: object
      properties:
        firstName:
          type: string
        lastName:
          type: string
        phoneNumber:
          type: string
        organization:
          type: string
        role:
          type: string
          enum: [ADMIN, ORGANIZER, ATTENDEE]

    UpdateProfileRequest:
      type: object
      properties:
        firstName:
          type: string
        lastName:
          type: string
        phoneNumber:
          type: string
        organization:
          type: string
        bio:
          type: string
          maxLength: 500
        profileImageUrl:
          type: string
          format: uri

    UserResponse:
      type: object
      properties:
        userId:
          type: string
          format: uuid
        email:
          type: string
          format: email
        firstName:
          type: string
        lastName:
          type: string
        role:
          type: string
          enum: [ADMIN, ORGANIZER, ATTENDEE]
        status:
          type: string
          enum: [ACTIVE, DEACTIVATED]
        createdAt:
          type: string
          format: date-time
        updatedAt:
          type: string
          format: date-time

    UserDetailResponse:
      allOf:
        - $ref: '#/components/schemas/UserResponse'
        - type: object
          properties:
            phoneNumber:
              type: string
            organization:
              type: string
            bio:
              type: string
            profileImageUrl:
              type: string
              format: uri
            emailVerified:
              type: boolean
            lastLoginAt:
              type: string
              format: date-time
            eventsOrganized:
              type: integer
            eventsAttended:
              type: integer

    # ============================================
    # EVENT MANAGEMENT SCHEMAS
    # ============================================
    CreateEventRequest:
      type: object
      required:
        - title
        - description
        - startDate
        - endDate
        - eventType
        - category
      properties:
        title:
          type: string
          minLength: 3
          maxLength: 200
        description:
          type: string
          minLength: 10
          maxLength: 5000
        startDate:
          type: string
          format: date-time
        endDate:
          type: string
          format: date-time
        eventType:
          type: string
          enum: [IN_PERSON, VIRTUAL, HYBRID]
        category:
          type: string
          enum: [CONFERENCE, WORKSHOP, SEMINAR, CONCERT, SPORTS, NETWORKING, CHARITY, OTHER]
        venueId:
          type: string
          format: uuid
          description: Required for IN_PERSON and HYBRID events
        virtualDetails:
          $ref: '#/components/schemas/VirtualEventDetails'
        tags:
          type: array
          items:
            type: string
          maxItems: 10
        maxAttendees:
          type: integer
          minimum: 1
        registrationStartDate:
          type: string
          format: date-time
        registrationEndDate:
          type: string
          format: date-time
        customFields:
          type: array
          items:
            $ref: '#/components/schemas/CustomFieldDefinition'
          description: Additional registration fields

    UpdateEventRequest:
      type: object
      properties:
        title:
          type: string
        description:
          type: string
        startDate:
          type: string
          format: date-time
        endDate:
          type: string
          format: date-time
        eventType:
          type: string
          enum: [IN_PERSON, VIRTUAL, HYBRID]
        category:
          type: string
        venueId:
          type: string
          format: uuid
        virtualDetails:
          $ref: '#/components/schemas/VirtualEventDetails'
        tags:
          type: array
          items:
            type: string
        maxAttendees:
          type: integer
        registrationStartDate:
          type: string
          format: date-time
        registrationEndDate:
          type: string
          format: date-time
        customFields:
          type: array
          items:
            $ref: '#/components/schemas/CustomFieldDefinition'

    VirtualEventDetails:
      type: object
      required:
        - platform
      properties:
        platform:
          type: string
          enum: [ZOOM, TEAMS, GOOGLE_MEET, WEBEX, CUSTOM]
        meetingUrl:
          type: string
          format: uri
        meetingId:
          type: string
        requiresPasscode:
          type: boolean
          default: false
        passcodeInstructions:
          type: string
        streamingUrl:
          type: string
          format: uri

    CustomFieldDefinition:
      type: object
      required:
        - fieldName
        - fieldType
        - required
      properties:
        fieldId:
          type: string
          format: uuid
        fieldName:
          type: string
        fieldLabel:
          type: string
        fieldType:
          type: string
          enum: [TEXT, NUMBER, EMAIL, PHONE, DATE, SELECT, MULTISELECT, TEXTAREA, CHECKBOX]
        required:
          type: boolean
        options:
          type: array
          items:
            type: string
          description: For SELECT and MULTISELECT types
        placeholder:
          type: string
        validationRules:
          type: object
          properties:
            minLength:
              type: integer
            maxLength:
              type: integer
            pattern:
              type: string
            min:
              type: number
            max:
              type: number

    EventSummary:
      type: object
      properties:
        eventId:
          type: string
          format: uuid
        title:
          type: string
        description:
          type: string
        startDate:
          type: string
          format: date-time
        endDate:
          type: string
          format: date-time
        eventType:
          type: string
        category:
          type: string
        bannerUrl:
          type: string
          format: uri
        venue:
          $ref: '#/components/schemas/VenueSummary'
        organizer:
          $ref: '#/components/schemas/OrganizerInfo'
        status:
          type: string
          enum: [DRAFT, PENDING_APPROVAL, PUBLISHED, CANCELLED, COMPLETED]
        tags:
          type: array
          items:
            type: string
        registrationStatus:
          type: string
          enum: [NOT_STARTED, OPEN, CLOSED, FULL]
        attendeeCount:
          type: integer
        maxAttendees:
          type: integer
        createdAt:
          type: string
          format: date-time

    EventResponse:
      allOf:
        - $ref: '#/components/schemas/EventSummary'
        - type: object
          properties:
            updatedAt:
              type: string
              format: date-time

    EventDetailResponse:
      allOf:
        - $ref: '#/components/schemas/EventResponse'
        - type: object
          properties:
            virtualDetails:
              $ref: '#/components/schemas/VirtualEventDetails'
            registrationStartDate:
              type: string
              format: date-time
            registrationEndDate:
              type: string
              format: date-time
            customFields:
              type: array
              items:
                $ref: '#/components/schemas/CustomFieldDefinition'
            ticketTiers:
              type: array
              items:
                $ref: '#/components/schemas/TicketTierResponse'
            coOrganizers:
              type: array
              items:
                $ref: '#/components/schemas/CoOrganizerInfo'

    OrganizerInfo:
      type: object
      properties:
        userId:
          type: string
          format: uuid
        firstName:
          type: string
        lastName:
          type: string
        organization:
          type: string
        profileImageUrl:
          type: string
          format: uri

    CoOrganizerInfo:
      allOf:
        - $ref: '#/components/schemas/OrganizerInfo'
        - type: object
          properties:
            role:
              type: string
              enum: [PRIMARY, CO_ORGANIZER]
            addedAt:
              type: string
              format: date-time

    # ============================================
    # VENUE MANAGEMENT SCHEMAS
    # ============================================
    CreateVenueRequest:
      type: object
      required:
        - name
        - address
      properties:
        name:
          type: string
          minLength: 2
          maxLength: 200
        description:
          type: string
          maxLength: 1000
        address:
          $ref: '#/components/schemas/Address'
        capacity:
          type: integer
          minimum: 1
        amenities:
          type: array
          items:
            type: string
        contactInfo:
          $ref: '#/components/schemas/ContactInfo'

    UpdateVenueRequest:
      type: object
      properties:
        name:
          type: string
        description:
          type: string
        address:
          $ref: '#/components/schemas/Address'
        capacity:
          type: integer
        amenities:
          type: array
          items:
            type: string
        contactInfo:
          $ref: '#/components/schemas/ContactInfo'

    Address:
      type: object
      required:
        - street
        - city
        - country
      properties:
        street:
          type: string
        street2:
          type: string
        city:
          type: string
        state:
          type: string
        postalCode:
          type: string
        country:
          type: string
        coordinates:
          $ref: '#/components/schemas/Coordinates'

    Coordinates:
      type: object
      required:
        - latitude
        - longitude
      properties:
        latitude:
          type: number