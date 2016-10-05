#pragma once
#include "ThirdOctave.h"
#include <functional>
#include <vector>
#include <memory>

class CThirdOctaveImpl;
class THIRDOCTAVE_API CToctEx
{
public:
	CToctEx(CThirdOctaveImpl* pImpl);
	void InitEx(std::function<void(const double*)> GetFrameLevels);
	void InitResultFunc(std::function<size_t()> GetResultCount, std::function<float*(size_t index)> GetResultPointer, std::function<int()> GetChannelCount);
	const double* GetCurrentFrame();
	double GetLevel(double power);
	int GetCalculateChannelCount();
private:
	CToctEx(const CToctEx&);
	CToctEx& operator = (const CToctEx&);
private:
	CThirdOctaveImpl* m_pImpl;
};
