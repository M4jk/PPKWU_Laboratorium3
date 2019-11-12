package com.i209344;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class CalendarController {

    @GetMapping("/calendar/{year}/{month}")
    @ResponseBody
    public ResponseEntity<Resource> getICalendar(@PathVariable int year, @PathVariable int month) throws IOException {

        String weeiaCalendarEndpoint="http://www.weeia.p.lodz.pl/pliki_strony_kontroler/kalendarz.php?rok=" + year + "&miesiac=" + month + "&lang=1";

        Document document = Jsoup.connect(weeiaCalendarEndpoint).get();
        Elements daysElements = document.select("a.active");
        Elements eventNamesElements = document.select("div.InnerBox");

        List<String> days = new ArrayList<>();
        List<String> eventNames = new ArrayList<>();

        daysElements.stream()
                    .forEach(dayElement -> days.add(dayElement.text()));

        eventNamesElements.stream()
                          .forEach(eventNameElement -> eventNames.add(eventNameElement.text()));

        ICalendar iCalendar = new ICalendar();

        for (int index = 0; index < days.size(); index++) {
            VEvent vEvent = new VEvent();
            vEvent.setSummary(eventNames.get(index));

            Calendar date = Calendar.getInstance();
            date.set(year, month - 1, Integer.parseInt(days.get(index)));

            vEvent.setDateStart(date.getTime());
            vEvent.setDateEnd(date.getTime());

            iCalendar.addEvent(vEvent);
        }

        File calendarFile = new File(""+ year + "calendarFor" + month + "Month.ics");
        Biweekly.write(iCalendar).go(calendarFile);

        Resource fileStreamResource = new FileSystemResource(calendarFile);
        return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/calendar"))
                    .body(fileStreamResource);
    }
}
