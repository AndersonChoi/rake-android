package com.rake.android.rkmetrics;

import com.rake.android.rkmetrics.MessageLoop.Command;
import static com.rake.android.rkmetrics.MessageLoop.Command.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19, manifest = Config.NONE)
public class MessageLoopSpec {

    @Test
    public void Command_cannot_have_same_code() {
        List<Command> commands = Arrays.asList(Command.values());
        Set<Integer> codes = new HashSet<Integer>();

        assertThat(commands.size()).isGreaterThan(0);

        for (Command c : commands) {
            assertThat(codes.contains(c.getCode())).isFalse();
            codes.add(c.getCode());
        }
    }
}
