package it.jacopocarlini.fff.controller;

import dev.openfeature.sdk.MutableContext;
import it.jacopocarlini.fff.models.FlagDetails;
import it.jacopocarlini.fff.service.FlagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FlagController {

    @Autowired
    private FlagService flagService;

    @GetMapping("/flags")
    List<FlagDetails> getFlags() {
        return flagService.getFlags();
    }

    @GetMapping("/flags/{key}")
    FlagDetails getFlag(@PathVariable String key) {
        return flagService.getFlag(key);
    }

    @PostMapping("/flags")
    @ResponseStatus(code = HttpStatus.CREATED)
    void createFlag(@RequestBody FlagDetails flagDetails) {
        flagService.crateFlag(flagDetails);
    }

    @PutMapping("/flags/{key}")
    void updateFlag(@PathVariable String key, @RequestBody FlagDetails flagDetails) {
        flagService.updateFlag(key, flagDetails);
    }

    @DeleteMapping("/flags/{key}")
    void deleteFlag(@PathVariable String key) {
        flagService.deleteFlag(key);
    }

    @GetMapping("/flags/{key}/eval")
    String evaluateFlag(@PathVariable String key,
                        @RequestParam(required = true) String defaultValue,
                        @RequestParam(required = false) String targetKey) {

        return flagService.getStringEvaluation(key, defaultValue, new MutableContext().setTargetingKey(targetKey));
    }


}
