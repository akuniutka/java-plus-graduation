INSERT INTO categories(name)
VALUES ('concerts'),
       ('cinemas');

INSERT INTO events(initiator_id, title, category_id, event_date, lat, lon, annotation, description, participant_limit,
                   paid, request_moderation, created_on, state)
VALUES (1, 'Concert', 1, '2100-12-31T23:59:59', 51.28, 0.0, 'First concert', 'You have been waiting for it', 0, false,
        false, '2000-01-01T00:00:01', 'PENDING');
