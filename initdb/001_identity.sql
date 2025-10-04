-- Schema publik; ganti ke "core" jika kamu pakai default_schema=core
CREATE TABLE IF NOT EXISTS public.warga (
                                            id           UUID        PRIMARY KEY,                 -- app set via @PrePersist
                                            nik          VARCHAR(16) NOT NULL UNIQUE
    CHECK (char_length(nik) = 16),
    nama         TEXT        NOT NULL,
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    alamat       TEXT,
    rt           INTEGER     CHECK (rt IS NULL OR rt BETWEEN 1 AND 999),
    rw           INTEGER     CHECK (rw IS NULL OR rw BETWEEN 1 AND 999),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),      -- @CreationTimestamp
    updated_at   TIMESTAMPTZ                              -- @UpdateTimestamp (diurus Hibernate)
    );

-- index yang aman (tanpa ekstensi khusus)
CREATE INDEX IF NOT EXISTS idx_warga_rt_rw ON public.warga (rt, rw);
CREATE INDEX IF NOT EXISTS idx_warga_nama_lower ON public.warga (lower(nama));