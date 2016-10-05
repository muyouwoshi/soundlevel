#pragma once

enum WindowType
{
	HAMMING,//					0
	HANNING,//					1
	RECTANGULAR,//				2
	KAISER_BESSEL,//			3
	FLAT_TOP,//				4
	BLACKMAN_HARRIS,//			5

	GAUSS_WINDOW,//			6
	EXPONENTIAL,//				7
	NUTTALL,//					8
	BARTLETT,//				9
	TUKEY,//					10
};

enum FreqWeighting
{
	NoWeighting,
	AWeighting,
	BWeighting,
	CWeighting,
};

enum TimeAvaraging
{
	SlowAverage,
	FastAverage,
	ImpsAverage,
	PeakAverage,
	LoudAverage,
	UserAverage,
	FlatAverage,
};

enum ThirdOctaveType
{
	TOCT_TYPE_ANSI_MAIN = 0,
	TOCT_TYPE_ANSI_SIDE,
	TOCT_TYPE_ANSI_ALL,
	TOCT_TYPE_IEC_MAIN = 0x0100,
	TOCT_TYPE_IEC_SIDE,
	TOCT_TYPE_IEC_ALL,
	TOCT_TYPE_LOUDNESS = 0xFF,
};

enum class SoundField
{
	FREE_FIELD,
	DIFFUSE_FIELD,
};

