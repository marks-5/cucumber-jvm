package cucumber.formatter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import cucumber.formatter.usage.UsageStatisticStrategy;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

public class UsageFormatterTest
{
    @Test
    public void close() throws IOException
    {
        Appendable out = mock(Appendable.class, withSettings().extraInterfaces(Closeable.class));
        UsageFormatter usageFormatter = new UsageFormatter(out);
        usageFormatter.close();
        verify((Closeable) out).close();
    }

    @Test
    public void resultWithoutSteps()
    {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        Result result = mock(Result.class);
        usageFormatter.result(result);
        verifyZeroInteractions(out);
    }

    @Test
    public void resultWithOneStep()
    {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);

        Step step = mock(Step.class);
        when(step.getName()).thenReturn("step");
        usageFormatter.step(step);

        Match match = mock(Match.class);
        usageFormatter.match(match);
        
        Result result = mock(Result.class);
        when(result.getDuration()).thenReturn(12345L);
        usageFormatter.result(result);

        Map<String,List<Long>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<Long> durationEntries = usageMap.get("step");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0), Long.valueOf(12));
    }

    @Test
    public void resultWithZeroDuration()
    {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);

        Step step = mock(Step.class);
        when(step.getName()).thenReturn("step");
        usageFormatter.step(step);

        Match match = mock(Match.class);
        usageFormatter.match(match);

        Result result = mock(Result.class);
        when(result.getDuration()).thenReturn(0L);
        usageFormatter.result(result);

        Map<String,List<Long>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<Long> durationEntries = usageMap.get("step");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0), Long.valueOf(0));
    }

    @Test
    public void resultWithNullDuration()
    {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);

        Step step = mock(Step.class);
        when(step.getName()).thenReturn("step");
        usageFormatter.step(step);

        Match match = mock(Match.class);
        usageFormatter.match(match);

        Result result = mock(Result.class);
        when(result.getDuration()).thenReturn(null);
        usageFormatter.result(result);

        Map<String,List<Long>> usageMap = usageFormatter.usageMap;
        assertEquals(usageMap.size(), 1);
        List<Long> durationEntries = usageMap.get("step");
        assertEquals(durationEntries.size(), 1);
        assertEquals(durationEntries.get(0), Long.valueOf(0));
    }

    @Test
    public void doneWithoutUsageStatisticStrategies() throws IOException
    {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);
        usageFormatter.usageMap.put("aStep", Arrays.asList(12345678L));

        usageFormatter.done();
        
        verify(out).append("\t12.345678 : aStep");
    }

    @Test
    public void doneWithUsageStatisticStrategies() throws IOException
    {
        Appendable out = mock(Appendable.class);
        UsageFormatter usageFormatter = new UsageFormatter(out);

        UsageStatisticStrategy usageStatisticStrategy = mock(UsageStatisticStrategy.class);
        when(usageStatisticStrategy.calculate(Arrays.asList(12345678L))).thenReturn(23456L);
        usageFormatter.addUsageStatisticStrategy("average", usageStatisticStrategy);
        
        usageFormatter.usageMap.put("aStep", Arrays.asList(12345678L));

        usageFormatter.done();

        verify(out).append("0.023456 (average) : aStep");
        verify(out).append("\t12.345678 : aStep");
    }
}
