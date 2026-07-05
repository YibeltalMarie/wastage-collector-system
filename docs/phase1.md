# Phase 1 — Project understanding: Ethiopia waste collector system

> This document is your thinking record before writing a single line of code.
> A real developer writes this first. It forces clarity, prevents wasted effort,
> and becomes the reference you return to throughout the entire build.

---

## 1. Define the problem

### What is the core problem?

In Ethiopian cities like Addis Ababa, waste management is largely informal and
unreliable. Residents have no way to schedule pickups, track whether a collector
is coming, or report missed collections. Collectors have no organized route or
digital record of their work. City administrators have no visibility into what
is actually happening on the ground.

The result: waste piles up, health risks increase, and nobody has accountability.

### What does this system solve?

This system creates a digital bridge between three groups — residents, waste
collectors, and administrators — so that:

- Residents can request pickups and know when to expect them
- Collectors receive organized, routed assignments and can update their status
- Administrators can monitor the entire operation and make data-driven decisions

### Why does this matter? (Context)

- Addis Ababa is a growing city with an increasing urban population
- Informal waste collection leads to health hazards, especially in dense kebeles
- Similar systems (like GREEN in Kenya) have proven this model works in African cities
- A digital system creates accountability that the current manual process cannot

### What is this system NOT?

It is not:
- A payment processing system (out of scope for v1)
- A GPS tracking app for collector vehicles in real time
- A government reporting tool with complex analytics (v1 keeps it simple)
- A social platform or community forum

> Defining what you are NOT building is just as important as defining what you are.
> Scope creep kills projects.

### Problem statement (one paragraph)

Residents in Addis Ababa lack a reliable, digital way to request and track waste
pickup. Collectors operate without organized assignments or digital tools.
Administrators cannot monitor service quality or identify problem areas. This
system addresses all three by providing a role-based platform where citizens
submit pickup requests, collectors manage their daily workload, and admins
oversee the full operation — creating accountability and reliability where
none currently exists.

---

## 2. Identify the users

A user is anyone who interacts with your system. You must know them deeply
before you can design anything for them.

---

### User 1 — Citizen (resident)

**Who are they?**
A resident of an Ethiopian city (primarily Addis Ababa) who generates household
or small-business waste and needs it collected regularly.

**Technical level:** Low to medium. May use a smartphone. Might prefer Amharic.

**What do they care about?**
- Knowing when their waste will be picked up
- Being able to request a pickup easily
- Getting notified when a collector is on the way or has completed the job
- Seeing a history of their past pickups

**Their frustrations today (without this system):**
- No idea when the collector will come
- No way to report a missed pickup
- No one to contact when service fails

---

### User 2 — Waste collector

**Who are they?**
A person employed by or contracted to a waste management company or municipality
to physically collect waste from households and businesses.

**Technical level:** Low to medium. Uses a mobile device in the field.

**What do they care about?**
- Knowing exactly where to go and in what order
- Marking pickups as done quickly
- Updating their availability status
- Not having to deal with complex software while working

**Their frustrations today (without this system):**
- Paper lists that get lost or are out of date
- No way to update residents when they are running late
- No record of completed work to show their employer

---

### User 3 — Administrator

**Who are they?**
A staff member of a waste management company or city authority who oversees the
operation — assigning collectors, monitoring performance, and managing the system.

**Technical level:** Medium to high. Works from a desktop or laptop.

**What do they care about?**
- Seeing all pending and completed requests in one dashboard
- Assigning or reassigning collectors to requests
- Tracking collector performance (how many pickups completed per day)
- Managing user accounts (registering new collectors, deactivating accounts)
- Identifying areas with high missed pickup rates

**Their frustrations today (without this system):**
- No visibility into what is happening in the field
- Complaints from residents with no way to verify them
- Manual paperwork and phone calls to coordinate collectors

---

## 3. User stories

A user story follows this format:

> **As a** [user], **I want to** [do something], **so that** [reason/benefit].

This format forces you to always connect a feature to a real human need.
If you cannot complete the "so that" part, the feature may not be necessary.

---

### Citizen user stories

#### What a citizen CAN do

| # | User story | Priority |
|---|-----------|----------|
| C-01 | As a citizen, I want to register an account with my name, phone number, and address, so that the system knows where I live and can identify me. | Must have |
| C-02 | As a citizen, I want to log in securely with my phone number and password, so that only I can access my account. | Must have |
| C-03 | As a citizen, I want to submit a waste pickup request with my location and a preferred date, so that a collector can be assigned to my area. | Must have |
| C-04 | As a citizen, I want to see the current status of my pickup request (pending, assigned, in progress, completed), so that I know what is happening. | Must have |
| C-05 | As a citizen, I want to view my pickup history, so that I can see when past collections happened. | Should have |
| C-06 | As a citizen, I want to cancel a pending pickup request I submitted, so that I can change my plans without wasting a collector's time. | Should have |
| C-07 | As a citizen, I want to update my profile information (phone, address), so that my details stay accurate. | Should have |
| C-08 | As a citizen, I want to receive a notification when my request is assigned to a collector, so that I know someone is coming. | Nice to have |
| C-09 | As a citizen, I want to see a simple map showing my location and the assigned collector's area, so that I have visual context. | Nice to have |

#### What a citizen CANNOT do

- Cannot see other citizens' pickup requests or personal information
- Cannot assign or reassign collectors
- Cannot view any admin dashboard or reports
- Cannot approve or reject collector applications
- Cannot modify their completed pickup records
- Cannot access another user's account
- Cannot submit a request on behalf of another citizen

---

### Collector user stories

#### What a collector CAN do

| # | User story | Priority |
|---|-----------|----------|
| CO-01 | As a collector, I want to log in with my credentials, so that I can access my assigned work. | Must have |
| CO-02 | As a collector, I want to see all pickup requests assigned to me, so that I know my workload for the day. | Must have |
| CO-03 | As a collector, I want to view the details of each assigned request (citizen name, address, notes), so that I know where to go. | Must have |
| CO-04 | As a collector, I want to mark a pickup as "in progress" when I start it, so that the citizen and admin can see I am on my way. | Must have |
| CO-05 | As a collector, I want to mark a pickup as "completed" after I finish it, so that the system records it as done. | Must have |
| CO-06 | As a collector, I want to mark a pickup as "unable to complete" with a reason, so that admins can follow up. | Should have |
| CO-07 | As a collector, I want to set my availability status (available / unavailable), so that admins know whether to assign me work. | Should have |
| CO-08 | As a collector, I want to view my own pickup history and performance summary, so that I can track my own work. | Nice to have |

#### What a collector CANNOT do

- Cannot see pickups assigned to other collectors
- Cannot assign requests to themselves — only admins do this
- Cannot create or delete citizen accounts
- Cannot access the admin dashboard
- Cannot modify a pickup record after marking it complete
- Cannot view payment or financial data (out of scope for v1)
- Cannot register new collectors

---

### Administrator user stories

#### What an admin CAN do

| # | User story | Priority |
|---|-----------|----------|
| A-01 | As an admin, I want to log in with admin credentials, so that I can access the management dashboard. | Must have |
| A-02 | As an admin, I want to see all pending pickup requests in a list, so that I can decide what needs to be assigned. | Must have |
| A-03 | As an admin, I want to assign a pending request to a specific collector, so that someone is responsible for completing it. | Must have |
| A-04 | As an admin, I want to reassign a request from one collector to another, so that I can handle absences or overload. | Must have |
| A-05 | As an admin, I want to register new collector accounts (name, phone, area), so that new staff can be onboarded. | Must have |
| A-06 | As an admin, I want to deactivate a collector account, so that former staff cannot access the system. | Must have |
| A-07 | As an admin, I want to view a dashboard with summary statistics (total requests, completed today, pending, missed), so that I can see the health ooff the operation. | Must have |
| A-08 | As an admin, I want to see a list of all collectors and their current status (available, unavailable, on duty), so that I know who can take new assignments. | Should have |
| A-09 | As an admin, I want to filter requests by status, date, or sub-city, so that I can find specific information quickly.  Should have |
| A-10 | As an admin, I want to view the full history of any specific pickup request, so that I can investigate complaints. | Should have |
| A-11 | As an admin, I want to register new citizen accounts manually, so that I can help citizens who cannot self-register. | Nice to have |

#### What an admin CANNOT do

- Cannot submit pickup requests as if they were a citizen
- Cannot mark a pickup as completed themselves (only collectors do this)
- Cannot delete completed pickup records (audit trail must be preserved)
- Cannot access another admin's account details
- Cannot change system-level configuration (that requires a super admin role, out of scope for v1)

---

## 4. System workflow

The workflow describes what happens in the system, step by step, from every
actor's perspective. This is the logic that will later become your API endpoints,
your service layer methods, and your database state changes.

---

### Workflow 1 — Citizen submits a pickup request

```
1. Citizen opens the app and logs in
2. Citizen navigates to "Request Pickup"
3. Citizen fills in:
      -  / addrLocationess (pre-filled from profile if available)
      - Preferred pickup date
      - Optional notes (e.g. "large item", "gate is locked")
4. Citizen submits the for|m
5. System creates a new PickupRequest record with status = PENDING
6. System sends a confirmation to the citizen (in-app notification)
7. Admin sees the new request appear in the dashboard
```

**States a pickup request moves through:**

```
PENDING → ASSIGNED → IN_PROGRESS → COMPLETED
                  ↘ CANCELLED (by citizen, if still PENDING)
                  ↘     · (by collector, with reason)
```

---

### Workflow 2 — Admin assigns a request to a collector

```
1. Admin opens the dashboard and sees PENDING requests
2. Admin views the details of a specific request
3. Admin selects an available collector from the list
4. Admin clicks "Assign"
5. System updates PickupRequest status = ASSIGNED
6. System links the collector to the request
7. Collector sees the new assignment appear in their task list
8. Citizen is notified: "Your request has been assigned"
```

---

### Workflow 3 — Collector completes a pickup

```
1. Collector logs in and views their assigned tasks
2. Collector selects a task and clicks "Start Pickup"
3. System updates status = IN_PROGRESS
4. Collector arrives at the location and collects the waste
5. Collector clicks "Mark as Completed"
6. System updates status = COMPLETED, records timestamp
7. Citizen is notified: "Your waste has been collected"
8. Admin dashboard reflects the updated completion count
```

---

### Workflow 4 — Collector cannot complete a pickup

```
1. Collector cannot reach the location or citizen is not available
2. Collector clicks "Cannot Complete" and selects a reason:
      - "Location not found"
      - "Gate locked / no access"
      - "Citizen not present"
      - "Other"
3. System updates status = FAILED, stores the reason
4. Admin is notified and can reassign or close the request
5. Citizen is notified with the failure reason
```

---

### Workflow 5 — Citizen cancels a pending request

```
1. Citizen opens their request list
2. Citizen finds a PENDING request they want to cancel
3. Citizen clicks "Cancel Request"
4. System checks: request must be in PENDING status
   - If PENDING → status = CANCELLED, record updated
   - If ASSIGNED or later → cancellation not allowed, show message
5. Citizen sees confirmation of cancellation
```

---

## 5. Key business rules

These are the rules the system must enforce. They inform your backend validation
logic, your service layer, and your database constraints.

| Rule | Description |
|------|-------------|
| BR-01 | A citizen can only have one PENDING or ASSIGNED request at a time |
| BR-02 | A request can only be cancelled if it is in PENDING status |
| BR-03 | Only an admin can assign a collector to a request |
| BR-04 | Only the assigned collector can update the status of their request |
| BR-05 | A collector must be in AVAILABLE status to be assigned new requests |
| BR-06 | A completed request cannot be edited or deleted |
| BR-07 | An admin cannot mark a request as completed — only the collector can |
| BR-08 | A citizen cannot see another citizen's requests |
| BR-09 | A collector cannot see requests assigned to other collectors |
| BR-10 | All status changes must be timestamped for audit purposes |

---

## 6. What you now know before writing code

By completing this document, you have answered:

- **What problem does this solve?** Unreliable, untracked waste collection in Addis Ababa
- **Who are the users?** Citizen, Collector, Admin — three distinct roles with different needs
- **What can each user do?** Clearly defined and written as user stories with priorities
- **What can each user NOT do?** Boundaries are explicit, which directly becomes your security layer
- **How does the system flow?** Five key workflows covering every major interaction
- **What are the business rules?** Ten rules that will live in your Spring Boot service layer

This is your contract with yourself. When you are deep in code and confused about
whether a feature belongs or how a status should change, you come back here.

---

*Document created: May 2026*
*Project: Ethiopia Waste Collector System*
*Stack: React + Spring Boot + PostgreSQL*
*Next step: System architecture diagram and database schema design*
