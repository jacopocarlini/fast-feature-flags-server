package it.jacopocarlini.fff.providers;

import it.jacopocarlini.fff.entity.Flag;
import it.jacopocarlini.fff.models.FlagDetails;
import it.jacopocarlini.fff.models.FlagItem;
import it.jacopocarlini.fff.repository.AssignedTargetRepository;
import it.jacopocarlini.fff.repository.FlagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlagService {

    @Autowired
    private FlagRepository flagRepository;

    @Autowired
    private AssignedTargetRepository assignedTargetRepository;

    public List<FlagItem> getFlags() {
        return flagRepository.findAll().parallelStream()
                .map(elem -> FlagItem.builder()
                        .enabled(elem.getEnabled())
                        .target(elem.getTarget() != null)
                        .flagKey(elem.getFlagKey())
                        .timeWindowStart(elem.getTimeWindowStart())
                        .timeWindowEnd(elem.getTimeWindowEnd())
                        .rollout(elem.getRolloutPercentage() != null)
                        .variants(elem.getVariants().size())
                        .build())
                .toList();
    }

    public FlagDetails getFlag(String flagKey) {
        Flag flag = getFlagIfIsPresent(flagKey);
        return FlagDetails.builder()
                .enabled(flag.getEnabled())
                .timeWindowStart(flag.getTimeWindowStart())
                .timeWindowEnd(flag.getTimeWindowEnd())
                // TODO
                .build();
    }

    public void crateFlag(Flag flag) {
        var isPresent = flagRepository.findFirstByFlagKey(flag.getFlagKey())
                .isPresent();
        if (isPresent) {
            throw new RuntimeException("conflict");
        }

        checkRolloutPercentage(flag);

        flagRepository.save(flag);
    }

    private static void checkRolloutPercentage(Flag flag) {
        int percentage = 0;
        for (var entry : flag.getRolloutPercentage().entrySet()) {
            percentage += entry.getValue();
        }
        if (percentage != 100) {
            throw new RuntimeException("bad request");

        }
    }

    public void updateFlag(String flagKey, Flag newFlag) {
        var flag = getFlagIfIsPresent(flagKey);

        checkRolloutPercentage(newFlag);

        newFlag.setId(flag.getId());

        if (newFlag.getRolloutPercentage().equals(flag.getRolloutPercentage())) {
            assignedTargetRepository.deleteAllByFlagKey(flagKey);
        }

        flagRepository.save(newFlag);
    }

    private Flag getFlagIfIsPresent(String flagKey) {
        var flag = flagRepository.findFirstByFlagKey(flagKey);
        if (flag.isEmpty()) {
            throw new RuntimeException("not found");
        }
        return flag.get();
    }

    public void deleteFlag(String flagKey) {
        flagRepository.deleteByFlagKey(flagKey);
    }


}
