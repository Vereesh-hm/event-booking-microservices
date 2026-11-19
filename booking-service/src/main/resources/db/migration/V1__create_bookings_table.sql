CREATE TABLE IF NOT EXISTS bookings (

    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    event_id UUID NOT NULL,

    number_of_seats INTEGER NOT NULL,

    booking_status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP NOT NULL,

    updated_at TIMESTAMP NOT NULL,

    CONSTRAINT chk_booking_seats CHECK (number_of_seats >= 1),

    CONSTRAINT chk_booking_status
        CHECK (booking_status IN ('CONFIRMED', 'CANCELLED'))
);