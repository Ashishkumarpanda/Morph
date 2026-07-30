package dev.morph.benchmarks;

import dev.morph.Mapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class MappingBenchmark {

    @State(Scope.Thread)
    public static class Data {
        User user = new User(1L, "Ada", "Lovelace");
    }

    record User(Long id, String firstName, String lastName) {
    }

    record UserDto(Long id, String firstName, String lastName) {
    }

    @Benchmark
    public UserDto morphMapping(Data data) {
        return Mapper.map(data.user, UserDto.class);
    }

    @Benchmark
    public UserDto manualMapping(Data data) {
        User user = data.user;
        return new UserDto(user.id(), user.firstName(), user.lastName());
    }
}
