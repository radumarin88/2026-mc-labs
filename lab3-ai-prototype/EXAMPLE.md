# Worked example (reference only)

This is a filled-in example of the Lab 3 deliverables for one made-up app, so you can see the depth
we expect, especially in the mobile-computing framing and the reflection. **Do not adopt this idea.**
Pick your own; the value of the lab is in choosing and defending your own app. This example uses a
different domain on purpose.

---

## Example `app-idea.md`

**App name:** TrailMark

**One-line pitch:** A logger for trail maintenance crews to record hazards along a path while out of
signal, and sync them to the ranger station once back in coverage.

**The problem:** Crews find fallen trees, washouts, and broken signs deep in areas with no cellular
coverage. Today they scribble notes and re-key them hours later, losing detail and location.

**Target users:** Volunteer and staff trail crews, in the field, often with no signal for hours.

**Core screen(s):** A list of the hazards logged today, and a form to add one (type, severity, note,
auto-filled GPS location and time).

**The main thing the app manages:** a Hazard record.

**A few fields:** id, type (tree, washout, sign, other), severity (1 to 3), note, latitude, longitude,
createdAt, syncedAt (null until uploaded).

**What records does the app create or collect?** Hazard reports created by the user, tagged with GPS
and time from the device.

**Where does data come from?** User input plus the device's location sensor.

**Where does data need to go?** Uploaded to the ranger station's server when connectivity returns;
until then it lives only on the phone.

### Mobile-computing framing

- **What must persist on the device?** Every hazard the crew logs, because they may not reach signal
  for hours and must not lose a single report if the app is closed or the phone restarts.
- **What crosses the network?** New hazards upload to the station API; the app may also pull the
  latest known hazards so crews do not double-report.
- **What must still work with no signal?** Everything except sync. Logging, viewing today's list, and
  capturing GPS all work fully offline; upload is deferred.
- **The placement question:** GPS smoothing and de-duplication (is this the same washout someone
  logged last week?) could run on-device or on the server. On-device keeps working offline and avoids
  sending raw location to the cloud (privacy), but a server has the full history to compare against.
  Likely answer: cheap de-dup on-device against today's local list, full de-dup on the server at sync
  time. Trade: on-device saves latency and bandwidth and preserves privacy, but cannot see the global
  history until it syncs.

---

## Example `reflection.md` (excerpt)

**What AI did well:** generated the hazard list and add-hazard form in Compose quickly, produced
believable sample data, and wired navigation between the two screens in minutes.

**What broke when we stressed it:**
- Rotate the device: the half-filled form reset to empty. State was held in a plain variable, not
  survived across configuration change.
- Kill and reopen: every logged hazard was gone. Nothing was saved anywhere.
- Airplane mode: no effect yet, because there is no network code at all, which is itself the problem:
  there is no upload and no notion of "not yet synced."
- 500 items: fine, because we used a lazy list from Lab 2.
- Two people editing: no concept of sync or conflict; two crews would create duplicate reports.

**What is missing for production:** persistence (Lab 6) is the most urgent, since losing field reports
defeats the app; then offline-aware upload (Lab 7), a real API (Lab 5), and structure to hold it all
together (Lab 4). 

**One sentence:** AI gave us screens that look like the app, but not the decisions about where data
lives, what happens with no signal, and how reports reach the station, and it cannot make those
decisions for us because they depend on our users' constraints, not on patterns it has seen before.
