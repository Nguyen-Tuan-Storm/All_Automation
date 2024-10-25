from datetime import date


print(date.today())

next_year = date.today().year + 1
new_year = date(next_year, 1, 1)
day_to_new_year = new_year - date.today()

print(type(day_to_new_year))
print(day_to_new_year)