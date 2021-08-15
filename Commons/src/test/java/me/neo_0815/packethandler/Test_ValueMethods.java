package me.neo_0815.packethandler;

import me.neo_0815.packethandler.Random.ValueRange;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.IPacketClassFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.temporal.ChronoField;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static me.neo_0815.packethandler.StreamUtils.generate;
import static me.neo_0815.packethandler.StreamUtils.zip;

public class Test_ValueMethods {
	private static final Random RAND = new Random();
	
	public static Stream<String> String() {
		return concat(
				static_String(),
				random_String(5)
		);
	}
	
	private static Stream<String> static_String() {
		return of(
				"Hello World!",
				"",
				"äöüÄÖÜßµł€¶ŧ←↓→øþð",
				"篚",
				"\uD83E\uDDDF\uD83E\uDDE0",
				"-".repeat(100)
		);
	}
	
	private static Stream<String> random_String(final long maxSize) {
		return generate(() -> RAND.chars(new ValueRange(1, 0xFFFF))
						.limit(RAND.nextInt(100))
						.map(String::valueOf)
						.collect(Collectors.joining())
				, maxSize);
	}
	
	public static Stream<BigInteger> BigInteger() {
		return concat(of(
				BigInteger.valueOf(Long.MIN_VALUE).multiply(BigInteger.TEN),
				BigInteger.valueOf(Long.MIN_VALUE),
				BigInteger.valueOf(-1),
				BigInteger.ZERO,
				BigInteger.ONE,
				BigInteger.valueOf(Long.MAX_VALUE),
				BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TEN)
		), RAND.longs().limit(5).mapToObj(BigInteger::valueOf));
	}
	
	public static Stream<BigDecimal> BigDecimal() {
		return concat(of(
				BigDecimal.valueOf(Long.MIN_VALUE).multiply(BigDecimal.TEN),
				BigDecimal.valueOf(Long.MIN_VALUE),
				BigDecimal.valueOf(-1),
				BigDecimal.ZERO,
				BigDecimal.ONE,
				BigDecimal.valueOf(Long.MAX_VALUE),
				BigDecimal.valueOf(Long.MAX_VALUE).multiply(BigDecimal.TEN)
		), RAND.doubles().limit(5).mapToObj(BigDecimal::valueOf));
	}
	
	public static Stream<UUID> UUID() {
		return generate(UUID::randomUUID, 10);
	}
	
	public static Stream<Instant> Instant() {
		return concat(of(
				Instant.MIN,
				Instant.ofEpochSecond(-1),
				Instant.ofEpochSecond(0),
				Instant.ofEpochSecond(1),
				Instant.now(),
				Instant.MAX
		), RAND.longs(new ValueRange(-31557014167219200L, 31556889864403199L))
				.limit(5)
				.map(Instant::ofEpochSecond));
	}
	
	public static Stream<LocalDate> LocalDate() {
		return concat(of(
				LocalDate.MIN,
				LocalDate.of(0, 1, 1),
				LocalDate.EPOCH,
				LocalDate.now(),
				LocalDate.MAX
		), random_LocalDate(5));
	}
	
	private static Stream<LocalDate> random_LocalDate(final long maxSize) {
		return generate(() -> LocalDate.of(
				randInt(convert(ChronoField.YEAR.range())),
				randInt(convert(ChronoField.MONTH_OF_YEAR.range())),
				randInt(convert(ChronoField.DAY_OF_MONTH.range()))
		), maxSize);
	}
	
	public static Stream<LocalTime> LocalTime() {
		return concat(of(
				LocalTime.MIN,
				LocalTime.of(0, 0, 0, 0),
				LocalTime.NOON,
				LocalTime.now(),
				LocalTime.MIDNIGHT,
				LocalTime.MAX
		), random_LocalTime(5));
	}
	
	private static Stream<LocalTime> random_LocalTime(final long maxSize) {
		return generate(() -> LocalTime.of(
				randInt(convert(ChronoField.HOUR_OF_DAY.range())),
				randInt(convert(ChronoField.MINUTE_OF_HOUR.range())),
				randInt(convert(ChronoField.SECOND_OF_MINUTE.range())),
				randInt(convert(ChronoField.NANO_OF_SECOND.range()))
		), maxSize);
	}
	
	public static Stream<LocalDateTime> LocalDateTime() {
		return concat(of(
				LocalDateTime.MIN,
				LocalDateTime.of(0, 1, 1, 0, 0, 0, 0),
				LocalDateTime.now(),
				LocalDateTime.MAX
		), random_LocalDateTime(5));
	}
	
	private static Stream<LocalDateTime> random_LocalDateTime(final long maxSize) {
		return zip(
				random_LocalDate(maxSize),
				random_LocalTime(maxSize),
				LocalDateTime::of
		);
	}
	
	public static Stream<ZoneOffset> ZoneOffset() {
		return concat(of(
				ZoneOffset.MIN,
				ZoneOffset.ofHours(-1),
				ZoneOffset.ofTotalSeconds(-1),
				ZoneOffset.UTC,
				ZoneOffset.ofTotalSeconds(1),
				ZoneOffset.ofHours(1),
				ZoneOffset.MAX
		), random_ZoneOffset(5));
	}
	
	private static Stream<ZoneOffset> random_ZoneOffset(final long maxSize) {
		return generate(() -> ZoneOffset.ofTotalSeconds(
				randInt(new ValueRange(-64800, 64800))
		), maxSize);
	}
	
	public static Stream<ZoneId> ZoneId() {
		return ZoneId.getAvailableZoneIds().stream().map(ZoneId::of);
	}
	
	private static Stream<ZoneId> random_ZoneId(final long maxSize) {
		return ZoneId().unordered().limit(maxSize);
	}
	
	public static Stream<ZonedDateTime> ZonedDateTime() {
		return concat(of(
				ZonedDateTime.ofInstant(LocalDateTime.MIN, ZoneOffset.MIN, ZoneId.of("UTC")),
				ZonedDateTime.now(),
				ZonedDateTime.ofInstant(LocalDateTime.MAX, ZoneOffset.MAX, ZoneId.of("UTC"))
		), zip(
				random_LocalDateTime(10),
				random_ZoneId(10),
				random_ZoneOffset(10),
				(ldt, zi, zo) -> ZonedDateTime.ofInstant(ldt, zo, zi)
		));
	}
	
	public static Stream<Duration> Duration() {
		return concat(of(
				Duration.ofSeconds(Long.MIN_VALUE),
				Duration.ofSeconds(-1),
				Duration.ZERO,
				Duration.ofSeconds(1),
				Duration.ofSeconds(Long.MAX_VALUE),
				Duration.ofSeconds(Long.MAX_VALUE, 999_999_999)
		), generate(() -> Duration.ofSeconds(
				randLong(ValueRange.LONG),
				randInt(new ValueRange(0, 999_999_999))
		), 5));
	}
	
	public static Stream<Period> Period() {
		return concat(of(
				Period.of(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
				Period.ZERO,
				Period.of(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)
		), generate(() -> Period.of(
				randInt(ValueRange.INT),
				randInt(ValueRange.INT),
				randInt(ValueRange.INT)
		), 5));
	}
	
	@SuppressWarnings("MagicConstant")
	public static Stream<Pattern> Pattern() {
		return concat(
				static_String().map(Pattern::compile),
				random_String(5).map(s -> Pattern.compile(s, randInt(new ValueRange(0, 511))))
		);
	}
	
	public static Stream<PacketBase<?>> Packet() {
		return of(TestPacketType.values())
				.map(IPacketClassFactory::packet)
				.map(Supplier::get);
	}
	
	
	private static int randInt(final ValueRange range) {
		return RAND.ints(range).findAny().orElse(0);
	}
	
	private static long randLong(final ValueRange range) {
		return RAND.longs(range).findAny().orElse(0L);
	}
	
	private static ValueRange convert(final java.time.temporal.ValueRange range) {
		return ValueRange.of(range);
	}
}
