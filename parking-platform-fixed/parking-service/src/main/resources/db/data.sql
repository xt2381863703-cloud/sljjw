-- parking lots
INSERT INTO parking_lot (id, tenant_id, name, address, opening_hours_json)
VALUES
    ('lot-001','t1','杭电停车场','Somewhere, SG','{"Mon-Fri":"08:00-22:00","Sat-Sun":"10:00-20:00"}')
    ON CONFLICT (id) DO NOTHING;

-- spaces
INSERT INTO parking_space (id, tenant_id, lot_id, code, type, status, updated_at)
VALUES
    ('sp-001','t1','lot-001','B1-001','EV','AVAILABLE', NOW()),
    ('sp-002','t1','lot-001','B1-002','EV','AVAILABLE', NOW()),
    ('sp-003','t1','lot-001','B1-003','NORMAL','AVAILABLE', NOW()),
    ('sp-004','t1','lot-001','B1-004','NORMAL','AVAILABLE', NOW()),
    ('sp-005','t1','lot-001','B1-005','NORMAL','AVAILABLE', NOW())
    ON CONFLICT (id) DO NOTHING;
