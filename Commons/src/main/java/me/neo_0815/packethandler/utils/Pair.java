package me.neo_0815.packethandler.utils;

import lombok.Value;

@Value
public class Pair<T, U> {
	T first;
	U second;
}
