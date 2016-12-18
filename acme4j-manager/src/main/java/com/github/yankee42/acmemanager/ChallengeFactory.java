package com.github.yankee42.acmemanager;

import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.challenge.Challenge;

public interface ChallengeFactory {
    Challenge createChallenge(Authorization auth, String domain);
}
