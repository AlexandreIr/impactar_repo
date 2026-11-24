CREATE TABLE users (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name                 VARCHAR(150) NOT NULL,
    email                VARCHAR(255) NOT NULL,
    password_hash        VARCHAR(100) NOT NULL,
    role                 VARCHAR(20)  NOT NULL,
    must_change_password BOOLEAN      NOT NULL DEFAULT TRUE,
    active               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_users_email       UNIQUE (email),
    CONSTRAINT ck_users_email_lower CHECK (email = lower(email)),
    CONSTRAINT ck_users_role        CHECK (role IN ('ADMIN', 'INSTRUCTOR', 'STUDENT'))
);


CREATE TABLE courses (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    instructor_id UUID          NOT NULL,
    title         VARCHAR(200)  NOT NULL,
    description   TEXT,
    thumbnail_url VARCHAR(1000),
    is_published  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_courses_instructor
        FOREIGN KEY (instructor_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_courses_instructor ON courses (instructor_id);
CREATE INDEX idx_courses_published  ON courses (created_at DESC) WHERE is_published;


CREATE TABLE modules (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id   UUID         NOT NULL,
    title       VARCHAR(200) NOT NULL,
    order_index INTEGER      NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_modules_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT ck_modules_order_index CHECK (order_index >= 0)
);

CREATE INDEX idx_modules_course_order ON modules (course_id, order_index);


CREATE TABLE lessons (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    module_id   UUID          NOT NULL,
    title       VARCHAR(200)  NOT NULL,
    description TEXT,                    -- texto complementar + links (Markdown)
    video_url   VARCHAR(1000),           -- YouTube não listado no MVP
    order_index INTEGER       NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_lessons_module
        FOREIGN KEY (module_id) REFERENCES modules (id) ON DELETE CASCADE,
    CONSTRAINT ck_lessons_order_index CHECK (order_index >= 0)
);

CREATE INDEX idx_lessons_module_order ON lessons (module_id, order_index);


CREATE TABLE enrollments (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL,
    course_id   UUID        NOT NULL,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_enrollments_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT uq_enrollments_user_course UNIQUE (user_id, course_id)
);

CREATE INDEX idx_enrollments_course ON enrollments (course_id);


CREATE TABLE lesson_progress (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID        NOT NULL,
    lesson_id    UUID        NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_lesson_progress_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_lesson_progress_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE,
    CONSTRAINT uq_lesson_progress_user_lesson UNIQUE (user_id, lesson_id)
);

CREATE INDEX idx_lesson_progress_lesson ON lesson_progress (lesson_id);
