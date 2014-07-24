package prometheus

enum Status {
    QUEUED      ('10-queued'),
    COPYING     ('20-copying'),
    COPIED      ('25-copied'),
    UNZIPPING   ('30-unzipping'),
    UNZIPPED    ('35-unzipped'),
    ANALYSING   ('40-analysing'),
    ANALYSED    ('45-analysed'),
    HARVESTING  ('50-harvesting'),
    HARVESTED   ('55-harvested'),
    TIDYING     ('60-tidying'),
    COMPLETE    ('70-complete'),
    ERROR       ('00-error')
    
    String value
    Status(String value) { this.value = value }
}
    
