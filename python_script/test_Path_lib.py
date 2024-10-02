from pathlib import Path
import time

path = Path('notes.md')
path.touch()
path.write_text("create text in file")
time.sleep(5)
path.rename("note.txt")
# path.unlink(missing_ok=True)