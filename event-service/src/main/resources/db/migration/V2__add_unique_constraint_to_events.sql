ALTER TABLE events
ADD CONSTRAINT uk_events_title_location_date
UNIQUE (title, location, event_date);
