-- name: save-message!
-- creates a new message
INSERT INTO workbook
(name, message, timestamp)
VALUES (:name, :message, :timestamp)

-- name: get-messages
-- select all available messages
SELECT * from workbook;

