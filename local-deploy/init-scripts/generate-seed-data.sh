#!/bin/bash

# Generate large seed data for Auth and Event databases

cat > auth-db-seed.sql << 'EOF'
-- Seed data for Auth Database with 50+ users
-- Password for all users: Test123! (bcrypt hashed)

INSERT INTO users (id, email, password, full_name, role, is_active, created_at, updated_at) VALUES
(1, 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin User', 'ADMIN', true, NOW(), NOW()),
EOF

# Generate 50 users
for i in {2..51}; do
  first_names=("John" "Jane" "Bob" "Alice" "Charlie" "Diana" "Evan" "Fiona" "George" "Hannah" "Ian" "Julia" "Kevin" "Laura" "Mike" "Nancy" "Oscar" "Paula" "Quinn" "Rachel" "Steve" "Tina" "Uma" "Victor" "Wendy" "Xavier" "Yara" "Zack")
  last_names=("Smith" "Johnson" "Williams" "Brown" "Jones" "Garcia" "Miller" "Davis" "Rodriguez" "Martinez" "Hernandez" "Lopez" "Gonzalez" "Wilson" "Anderson" "Thomas" "Taylor" "Moore" "Jackson" "Martin" "Lee" "Perez" "Thompson" "White" "Harris" "Sanchez" "Clark" "Ramirez")
  
  first_idx=$((i % 28))
  last_idx=$(((i * 7) % 28))
  
  first=${first_names[$first_idx]}
  last=${last_names[$last_idx]}
  email=$(echo "${first}.${last}${i}@example.com" | tr '[:upper:]' '[:lower:]')
  
  echo "($i, '$email', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '$first $last', 'USER', true, NOW(), NOW())," >> auth-db-seed.sql
done

# Remove last comma and add conflict clause
sed -i '' '$ s/,$//' auth-db-seed.sql
echo "ON CONFLICT (email) DO NOTHING;" >> auth-db-seed.sql

# Generate profiles
echo "" >> auth-db-seed.sql
echo "INSERT INTO profiles (user_id, phone_number, address, profile_image_url, created_at, updated_at) VALUES" >> auth-db-seed.sql

for i in {1..51}; do
  phone="+1-555-$(printf "%04d" $i)"
  street=$((100 + i * 10))
  img=$((i % 70))
  echo "($i, '$phone', '$street Market St, San Francisco, CA 941$(printf "%02d" $((i % 50)))', 'https://i.pravatar.cc/150?img=$img', NOW(), NOW())," >> auth-db-seed.sql
done

sed -i '' '$ s/,$//' auth-db-seed.sql
echo "ON CONFLICT (user_id) DO NOTHING;" >> auth-db-seed.sql

# Generate user stats
echo "" >> auth-db-seed.sql
echo "INSERT INTO user_event_stats (user_id, total_events_created, total_events_attended, last_updated_at) VALUES" >> auth-db-seed.sql

for i in {1..51}; do
  created=$((i % 10))
  attended=$((i % 20))
  echo "($i, $created, $attended, NOW())," >> auth-db-seed.sql
done

sed -i '' '$ s/,$//' auth-db-seed.sql
echo "ON CONFLICT (user_id) DO NOTHING;" >> auth-db-seed.sql

# Set sequences
cat >> auth-db-seed.sql << 'EOF'

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('profiles_id_seq', (SELECT MAX(id) FROM profiles));
SELECT setval('user_event_stats_id_seq', (SELECT MAX(id) FROM user_event_stats));
EOF

echo "✅ Generated auth-db-seed.sql with 51 users"

# Generate event seed data
cat > event-db-seed.sql << 'EOF'
-- Seed data for Event Database with 100+ events

INSERT INTO event_schema.events (id, title, description, start_date, end_date, location, organizer_id, status, created_at, updated_at, max_attendees, current_attendees) VALUES
EOF

event_titles=(
  "Tech Conference" "Startup Pitch Night" "AI Workshop" "Networking Mixer" "Hackathon"
  "Cloud Architecture Masterclass" "Women in Tech Summit" "Blockchain Meetup" "DevOps Best Practices"
  "Product Management Workshop" "Cybersecurity Conference" "Mobile App Development" "Data Science Symposium"
  "UX/UI Design Sprint" "Founders Dinner" "Open Source Day" "Quantum Computing Seminar"
  "Agile Workshop" "Career Fair" "API Design Workshop" "Machine Learning Bootcamp" "Startup Weekend"
  "Code Review Session" "Tech Talk Series" "Innovation Summit"
)

locations=(
  "Moscone Center, San Francisco" "WeWork Mission St" "TechHub Market St" "The View Lounge"
  "GitHub HQ" "AWS Office" "Palace Hotel" "Coinbase Office" "Docker HQ" "Airbnb Office"
  "Salesforce Tower" "General Assembly" "IDEO Office" "Mozilla Office" "IBM Research"
  "Atlassian Office" "Fort Mason Center" "Postman Office" "Google Campus" "Meta Office"
)

for i in {1..100}; do
  title_idx=$((i % 25))
  loc_idx=$((i % 20))
  organizer=$((2 + (i % 49)))
  
  title="${event_titles[$title_idx]} $(date -v+${i}d +%Y)"
  location="${locations[$loc_idx]}"
  
  start_date=$(date -v+${i}d -v+9H +"%Y-%m-%d %H:%M:%S")
  end_date=$(date -v+${i}d -v+17H +"%Y-%m-%d %H:%M:%S")
  
  max_attendees=$((50 + (i * 10) % 500))
  current=$((max_attendees * (30 + (i % 50)) / 100))
  
  desc="Join us for an amazing ${event_titles[$title_idx]} event. Network with professionals and learn new skills."
  
  echo "($i, '$title', '$desc', '$start_date', '$end_date', '$location', $organizer, 'PUBLISHED', NOW(), NOW(), $max_attendees, $current)," >> event-db-seed.sql
done

sed -i '' '$ s/,$//' event-db-seed.sql
echo "ON CONFLICT (id) DO NOTHING;" >> event-db-seed.sql

# Generate categories
echo "" >> event-db-seed.sql
echo "INSERT INTO event_schema.event_categories (event_id, category) VALUES" >> event-db-seed.sql

categories=("TECHNOLOGY" "BUSINESS" "NETWORKING" "WORKSHOP" "CONFERENCE" "SOCIAL" "HACKATHON" "DESIGN")

for i in {1..100}; do
  cat1_idx=$((i % 8))
  cat2_idx=$(((i + 3) % 8))
  echo "($i, '${categories[$cat1_idx]}'), ($i, '${categories[$cat2_idx]}')," >> event-db-seed.sql
done

sed -i '' '$ s/,$//' event-db-seed.sql
echo "ON CONFLICT DO NOTHING;" >> event-db-seed.sql

# Generate attendees
echo "" >> event-db-seed.sql
echo "INSERT INTO event_schema.event_attendees (event_id, user_id, registration_date, attendance_status) VALUES" >> event-db-seed.sql

for event_id in {1..100}; do
  num_attendees=$((5 + (event_id % 15)))
  for j in $(seq 1 $num_attendees); do
    user_id=$((2 + ((event_id * j) % 49)))
    days_ago=$((1 + (j % 30)))
    echo "($event_id, $user_id, NOW() - INTERVAL '$days_ago days', 'CONFIRMED')," >> event-db-seed.sql
  done
done

sed -i '' '$ s/,$//' event-db-seed.sql
echo "ON CONFLICT DO NOTHING;" >> event-db-seed.sql

# Set sequences
cat >> event-db-seed.sql << 'EOF'

SELECT setval('event_schema.events_id_seq', (SELECT MAX(id) FROM event_schema.events));
EOF

echo "✅ Generated event-db-seed.sql with 100 events"
echo ""
echo "Seed files created successfully!"
echo "- auth-db-seed.sql (51 users)"
echo "- event-db-seed.sql (100 events)"
