SELECT Within(boundary, footprint)
FROM named_places, buildings
WHERE named_places.name = 'Ashton'
AND buildings.address = '215 Main Street';
