#pragma once
#ifdef _MSC_VER
#ifdef ORDERANALYSIS_EXPORTS
#define ORDER_API _declspec(dllexport) 
#else
#define  ORDER_API _declspec(dllimport)
#endif
#else
#define ORDER_API 
#endif

#include <Common.h>
#include <stdint.h>
struct OrderParams
{
	double samplerate;
	double minOrder;
	double maxOrder;
	double spacing;
	double resolution;
	WindowType type;//hanning rectangular kaiser_bessel
	int time_interval;//ms
	double range;
	int dataType;//0 --- int24 1---float
};

inline void InitOrderParams(OrderParams& param)
{
	param.samplerate = 16000;
	param.type = WindowType::HANNING;
	param.minOrder = 0;
	param.maxOrder = 10;
	param.spacing = 0.1;
	param.resolution = 0.1;
	param.time_interval = 2;
	param.range = 102;
	param.dataType = 0;
}

class ORDER_API COrderAnalysis
{
public:
	explicit COrderAnalysis(OrderParams params);
	~COrderAnalysis();
	COrderAnalysis(const COrderAnalysis&) = delete;
	COrderAnalysis& operator= (const COrderAnalysis&) = delete;
	void SetRPMData(const float* rpm, int count, float samplerate);
	bool Calculate(const int* data, int samples);//whole file
	bool Calculate(const float* data, int samples);
	//return value
	//-1 not in calculate
	//0--100 calculate progress
	//1000 calculate finished
	int GetProgress();
	uint32_t GetResultSize();
	bool GetResultRPMInfo(uint32_t& startRPM, uint32_t& stopRPM, uint32_t& deltaRPM);
	bool GetResult(double order, float* result);
private:
	class COrderAnalysisImpl;
	COrderAnalysisImpl* m_pImpl;
};
