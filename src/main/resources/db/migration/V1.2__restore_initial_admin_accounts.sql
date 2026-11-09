-- Consolidated initial accounts: 2 admins and 5 test users.
-- Shared password: Project_Hackathon_23412_ (individually BCrypt-hashed).
-- Existing account passwords, details and balances are preserved.
INSERT INTO profile (
    id, name, surname, username, password,
    phone_number, status, visible, blocked,
    coin_balance, total_earned_coins, report_count, streak_days, created_date
)
VALUES
    ('dd225012-d998-452e-a9c9-c63049da85ad', 'Admin1', 'Adminov', 'tzadmin@gmail.com',
     '$2a$10$ZgGhFhO7.Pg3EvhxcfT30O.xkyzFg2BPFNomxVDvMpVtKs.1hh5rW',
     '+998935224774', 'ACTIVE', TRUE, FALSE, 0, 0, 0, 0, NOW()),
    ('234edeba-92a8-490c-bbb2-9aafaf921b96', 'Admin2', 'Adminov', 'tzadmin2711@gmail.com',
     '$2a$10$f6tsAfUa5JM6.t6vcTc.c.EJEBQasg7lONlMA8rA5sxFeqHFoJ2Ga',
     '+998770644774', 'ACTIVE', TRUE, FALSE, 0, 0, 0, 0, NOW()),
    ('4a4d9b20-1865-434c-bf11-592040d9c38f', 'Sardor', 'Nurmatov', 'user1@gmail.com',
     '$2a$10$M1Q1AScO4FidAZXUXQE3K.RQepeFyG3DQsZhkbJ0AyenivcR6aWQS',
     '+998906122261', 'ACTIVE', TRUE, FALSE, 0, 0, 0, 0, NOW()),
    ('c0604910-d656-4997-9fef-d014fd3ecd94', 'Faxriddin', 'Saxlayev', 'user2@gmail.com',
     '$2a$10$nD6k9ClL96.uNtLVuSrDD.sutyD5Mjo/cDT7Icg7.EwRZ0/Dm2KDG',
     '+998881455121', 'ACTIVE', TRUE, FALSE, 0, 0, 0, 0, NOW()),
    ('cabf8af2-c34a-4843-b140-7ce37374d194', 'Azizbek', 'Abdimuratov', 'user3@gmail.com',
     '$2a$10$7n5ZPwmKpDWSaOdkYf5AveahUs6makV9KQdMQjp6FjAnEzCRKcRbO',
     '+998770651525', 'ACTIVE', TRUE, FALSE, 0, 0, 0, 0, NOW()),
    ('9118bd19-7838-4eaf-81cf-bbcccdef437c', 'Mashxurbek', 'Olimov', 'user4@gmail.com',
     '$2a$10$bHa2jcPBEZ1gScSLkeffyOOxZ.yBL1npYhiCM1BcqrYwUybgTXhdi',
     '+998934999971', 'ACTIVE', TRUE, FALSE, 0, 0, 0, 0, NOW()),
    ('f3180721-dc3c-4d16-bf47-1490c2f8c2e5', 'Lochinbek', 'Abdullayev', 'user5@gmail.com',
     '$2a$10$b8HvoRqkpG6lIEWk.qpyV.MlRgJKoAT854Y08wz/A3/qxP/ynt98m',
     '+998906328778', 'ACTIVE', TRUE, FALSE, 0, 0, 0, 0, NOW())
ON CONFLICT DO NOTHING;

INSERT INTO profile_role (id, profile_id, roles, created_date)
SELECT seed.role_id, p.id, seed.role, NOW()
FROM (VALUES
    ('a6e81a44-9a7c-4e9c-b6fa-904b978d4e6f', 'dd225012-d998-452e-a9c9-c63049da85ad', 'tzadmin@gmail.com', 'ROLE_ADMIN'),
    ('0b841b66-1b1c-4bff-9190-6c9774678c96', '234edeba-92a8-490c-bbb2-9aafaf921b96', 'tzadmin2711@gmail.com', 'ROLE_ADMIN'),
    ('d350f34c-a248-4ad2-a7fb-7aeddfb8ad02', '4a4d9b20-1865-434c-bf11-592040d9c38f', 'user1@gmail.com', 'ROLE_USER'),
    ('59ddc6a2-23ed-4232-b315-230408fe7bed', 'c0604910-d656-4997-9fef-d014fd3ecd94', 'user2@gmail.com', 'ROLE_USER'),
    ('8691d4d1-89cb-4400-b17f-cbd0763e6be2', 'cabf8af2-c34a-4843-b140-7ce37374d194', 'user3@gmail.com', 'ROLE_USER'),
    ('76aa37c8-ab27-4a83-9515-aca31464e2ef', '9118bd19-7838-4eaf-81cf-bbcccdef437c', 'user4@gmail.com', 'ROLE_USER'),
    ('a33a1b74-8546-40b9-872b-699701eacbae', 'f3180721-dc3c-4d16-bf47-1490c2f8c2e5', 'user5@gmail.com', 'ROLE_USER')
) AS seed(role_id, profile_id, username, role)
JOIN profile p ON p.id = seed.profile_id AND LOWER(p.username) = LOWER(seed.username)
WHERE NOT EXISTS (
    SELECT 1 FROM profile_role existing
    WHERE existing.profile_id = p.id AND existing.roles = seed.role
)
ON CONFLICT DO NOTHING;
