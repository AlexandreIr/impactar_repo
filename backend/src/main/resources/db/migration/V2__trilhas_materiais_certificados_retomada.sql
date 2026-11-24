-- =====================================================================
-- V2__trilhas_materiais_certificados_retomada.sql
--
-- ⚠ PROPOSTA — AGUARDANDO APROVAÇÃO.
--   NÃO copiar para db/migration antes de aprovar.
--   Destino após aprovação:
--   backend/src/main/resources/db/migration/V2__trilhas_materiais_certificados_retomada.sql
--
-- Cobre itens do escopo que NÃO têm tabela no escopo-mvp.md (seção 4):
--   1. Retomar de onde parou (pilar 3)            -> lesson_progress
--   2. Trilhas (pilar 3 + definição de "pronto")  -> trails, trail_courses, trail_enrollments
--   3. Arquivos para download (pilar 2)           -> lesson_materials
--   4. Certificados (pilar 4)                     -> certificates + courses.workload_hours
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. Retomada: linha criada na 1ª reprodução, completed_at preenchido ao concluir.
--    "Continuar de onde parou" = registro mais recente (updated_at) do aluno no curso.
-- ---------------------------------------------------------------------
ALTER TABLE lesson_progress
    ALTER COLUMN completed_at DROP NOT NULL,
    ALTER COLUMN completed_at DROP DEFAULT,
    ADD COLUMN last_position_seconds INTEGER     NOT NULL DEFAULT 0,
    ADD COLUMN updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD CONSTRAINT ck_lesson_progress_position CHECK (last_position_seconds >= 0);

CREATE INDEX idx_lesson_progress_user_recent ON lesson_progress (user_id, updated_at DESC);


-- ---------------------------------------------------------------------
-- 2. Trilhas
-- ---------------------------------------------------------------------
CREATE TABLE trails (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    title         VARCHAR(200)  NOT NULL,
    description   TEXT,
    thumbnail_url VARCHAR(1000),
    is_published  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_by    UUID          NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_trails_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_trails_published ON trails (created_at DESC) WHERE is_published;

-- PK surrogate (UUID) em vez de chave composta: mapeamento JPA mais simples
CREATE TABLE trail_courses (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    trail_id    UUID    NOT NULL,
    course_id   UUID    NOT NULL,
    order_index INTEGER NOT NULL,

    CONSTRAINT fk_trail_courses_trail
        FOREIGN KEY (trail_id) REFERENCES trails (id) ON DELETE CASCADE,
    CONSTRAINT fk_trail_courses_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT uq_trail_courses_trail_course UNIQUE (trail_id, course_id),
    CONSTRAINT ck_trail_courses_order_index CHECK (order_index >= 0)
);

CREATE INDEX idx_trail_courses_course ON trail_courses (course_id);

CREATE TABLE trail_enrollments (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL,
    trail_id    UUID        NOT NULL,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_trail_enrollments_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_trail_enrollments_trail
        FOREIGN KEY (trail_id) REFERENCES trails (id) ON DELETE CASCADE,
    CONSTRAINT uq_trail_enrollments_user_trail UNIQUE (user_id, trail_id)
);

CREATE INDEX idx_trail_enrollments_trail ON trail_enrollments (trail_id);


-- ---------------------------------------------------------------------
-- 3. Materiais complementares (links do Google Drive no MVP)
-- ---------------------------------------------------------------------
CREATE TABLE lesson_materials (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id   UUID          NOT NULL,
    title       VARCHAR(200)  NOT NULL,
    url         VARCHAR(1000) NOT NULL,
    order_index INTEGER       NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT fk_lesson_materials_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE,
    CONSTRAINT ck_lesson_materials_order_index CHECK (order_index >= 0)
);

CREATE INDEX idx_lesson_materials_lesson ON lesson_materials (lesson_id, order_index);


-- ---------------------------------------------------------------------
-- 4. Certificados
--    PDF gerado sob demanda a partir dos campos snapshot (sem armazenar arquivo).
--    Snapshot garante que o certificado emitido não muda se o curso for editado.
-- ---------------------------------------------------------------------
ALTER TABLE courses
    ADD COLUMN workload_hours INTEGER,
    ADD CONSTRAINT ck_courses_workload_hours CHECK (workload_hours IS NULL OR workload_hours > 0);

CREATE TABLE certificates (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID         NOT NULL,
    course_id         UUID,
    trail_id          UUID,
    verification_code VARCHAR(20)  NOT NULL,
    holder_name       VARCHAR(150) NOT NULL,   -- snapshot do nome do aluno
    title             VARCHAR(200) NOT NULL,   -- snapshot do título do curso/trilha
    workload_hours    INTEGER,                 -- snapshot da carga horária
    issued_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_certificates_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_certificates_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE RESTRICT,
    CONSTRAINT fk_certificates_trail
        FOREIGN KEY (trail_id) REFERENCES trails (id) ON DELETE RESTRICT,
    CONSTRAINT uq_certificates_verification_code UNIQUE (verification_code),
    -- exatamente um alvo: curso OU trilha
    CONSTRAINT ck_certificates_target CHECK ((course_id IS NULL) <> (trail_id IS NULL))
);

CREATE UNIQUE INDEX uq_certificates_user_course
    ON certificates (user_id, course_id) WHERE course_id IS NOT NULL;
CREATE UNIQUE INDEX uq_certificates_user_trail
    ON certificates (user_id, trail_id) WHERE trail_id IS NOT NULL;
