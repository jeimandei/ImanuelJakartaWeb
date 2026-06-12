BEGIN;

-- Add Google Maps embed and link URLs to site_settings.
-- church_map_embed_url: the iframe src for the embedded map on /contact and /services.
--   To get the precise pb= URL: open Google Maps → navigate to the church → Share → Embed a map → copy src="...".
--   The default value below uses a search-based embed (no API key required) that works immediately.
-- church_map_link_url: the href for "Open in Google Maps" / "Get Directions" buttons.
INSERT INTO site_settings (setting_key, setting_value, description) VALUES
    ('church_map_embed_url',
     'https://maps.google.com/maps?q=GMIM+Imanuel+Jakarta&t=&z=17&ie=UTF8&iwloc=&output=embed',
     'Google Maps iframe src URL — use Share → Embed a map in Google Maps for the best result'),
    ('church_map_link_url',
     'https://maps.app.goo.gl/5HjLYiw4aH42j3Z96',
     'Direct Google Maps link for "Open in Google Maps" and "Get Directions" buttons')
ON CONFLICT (setting_key) DO NOTHING;

COMMIT;
