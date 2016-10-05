#pragma once

#ifdef THIRDOCTAVE_EXPORTS
#define THIRDOCTAVE_API
#else
#define THIRDOCTAVE_API
#endif

typedef void* HTHIRDOCTAVE;
#include <stdint.h>
#include "Common.h"


enum ThirdOctaveAvarageMethod
{
	TAU_EQUAL_TIME,
	TAU_EQUAL_CONF,
	TAU_VIPER_AUTO,
	TAU_LOUDNESS,
	TAU_IS_ZERO,
};


struct ThirdOctaveParam
{
//	double samplerate; samplerate 必须是48000 
	ThirdOctaveType type;
	ThirdOctaveAvarageMethod method;
	TimeAvaraging avarage; // slow ---- time_constant = 1.0 
						//fast ---- time_const = 0.125
						//user ----- use time_const value
	double time_constant;//持续时间
	int time_interval;// 毫秒，间隔时间
	double range;// read from signal file
	bool delay_flag;
};

inline void InitThirdOctaveParam(ThirdOctaveParam& param)
{
	param.type = TOCT_TYPE_IEC_MAIN;
	param.delay_flag = 0;
	param.method = TAU_VIPER_AUTO;
	param.time_constant = 0.0028;
	param.avarage = UserAverage;
	param.time_interval = 2;
	param.range = 93;
}

class CToctEx;

class THIRDOCTAVE_API CThirdOctave
{
public:
	CThirdOctave(ThirdOctaveParam param);
	~CThirdOctave();
	//sample size is 24 bit
	int Calculate(const int* data, uint32_t samples);
	uint32_t GetResultInfo(uint32_t* channels);
	const float* GetResult(uint32_t index);
	const float* GetMeanResult(uint32_t index);
	void GetFinalMean(const float* data, float* output, uint32_t count);
	void SetWeighting(FreqWeighting weighting);
	CToctEx* GetEx();
	void ReleaseEx(CToctEx* pEx);
	ThirdOctaveParam GetM_param();
	void SetM_param(ThirdOctaveParam param);
private:
	CThirdOctave(const CThirdOctave&);
	CThirdOctave& operator = (const CThirdOctave&);
private:
	void* m_pImpl;
};


//#ifdef __cplusplus
//extern "C"
//{
//#endif
//	HTHIRDOCTAVE THIRDOCTAVE_API InitThirdOctave(ThirdOctaveParam param);
//	//sample size is 24 bit
//	int THIRDOCTAVE_API CalculateThirdOctave(HTHIRDOCTAVE handle, const int32_t* buffer, uint32_t samples);
//	void THIRDOCTAVE_API UnInitThirdOctave(HTHIRDOCTAVE handle);
//	
//	//result get
//	int THIRDOCTAVE_API GetThirdOctaveCalcResultCount(HTHIRDOCTAVE handle, uint32_t* count, uint32_t* frame_size);
//	int THIRDOCTAVE_API GetThirdOctaveCalcResult(HTHIRDOCTAVE handle, uint32_t index, float* frame);
//	int THIRDOCTAVE_API GetThirdOctaveMeanMaxResultCount(HTHIRDOCTAVE handle, uint32_t* count_time, uint32_t* count_freq);
//	int THIRDOCTAVE_API GetThirdOctaveMeanMaxResult(HTHIRDOCTAVE handle, bool bMean, FreqWeighting weighting, double* data_time, double* data_frq);
//#ifdef __cplusplus
//}
//#endif
