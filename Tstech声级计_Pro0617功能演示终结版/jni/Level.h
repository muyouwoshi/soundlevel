#pragma once

#ifdef LEVEL_EXPORTS
#define LEVEL_API
#else
#define LEVEL_API
#endif

#include <stdint.h>

#include "Common.h"

//samplerate ��С��32000 
//range ���ļ����
//time_interval ʱ���� ms
struct LevelParam
{
	double samplerate;
	double range;
	FreqWeighting weighting;
	TimeAvaraging avarage;
	int time_interval;
};

struct  LevelStatistic
{
	float min;
	float max;
	float mean;
};
inline void InitLevelParam(LevelParam& param)
{
	param.samplerate=48000;
	param.time_interval=2;
	param.range = 93;
	param.weighting=NoWeighting;//NOWeighting 0,AWeighting 1,BWeighting 2,CWeighting 3
	param.avarage=FastAverage;//SlowAverage 0,FastAverage 1,ImpsAverage 2,PeakAverage 3,LoudAverage 4,UserAverage 5,FlatAverage 6
}
class LEVEL_API CLevel
{
public:
	CLevel(LevelParam param);
	~CLevel();
	//����24bit data
	int Calculate(const int* data, int samples);
	const double* GetResult(uint32_t* count);
	LevelStatistic GetStatistic();
	LevelParam GetM_param();
	void SetM_param(LevelParam param);
	void ResetResult();

	double GetSamplerate();
		double GetRange();
		int GetWeighting();
		int  GetAvarage();
		int GetTime_interval();
		void SetRange(double range,LevelParam param){
			param.range=range;
		}
private:
	CLevel(const CLevel&);
	CLevel& operator = (const CLevel&);
private:
	void* m_pImpl;
};

//typedef void* HLEVEL;

#ifdef __cplusplus
extern "C"
{
#endif

//HLEVEL LEVEL_API InitLevel(LevelParam param);
//int LEVEL_API CalculateLevel(HLEVEL handle, const int* buffer, uint32_t samples);
//uint32_t LEVEL_API GetLevelResult(HLEVEL handle, double* result, double* pMax, double* pMin, double* mean);
//void LEVEL_API UninitLevel(HLEVEL handle);

double LEVEL_API GetAbcMagnitude(double f, FreqWeighting weighting);

#ifdef __cplusplus
}
#endif
