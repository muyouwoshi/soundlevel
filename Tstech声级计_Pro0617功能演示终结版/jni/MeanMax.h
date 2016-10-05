#pragma once

#include <numeric>
#include <cmath>
#include <algorithm>
#include <vector>
#include <cstdint>

class CMeanMax
{
public:
	CMeanMax(int nChannels, bool bMean) 
		: m_channels(nChannels), 
		m_bCalcMean(bMean),
		m_bChannelsDivided(false)
	{
		m_mean_channels.assign(nChannels, 0.0f);
	}
	void AddData(const float* data)
	{
		float total;
		if (m_bCalcMean)
		{
			total = std::accumulate(data, data + m_channels, 0.0f);
			for (int i = 0; i != m_channels; ++i)
				m_mean_channels[i] += data[i];
		}
		else
		{
			total = *(std::max_element(data, data+m_channels, 
				[](float a, float b){
				if (abs(a) < abs(b))
					return true;
				return false;
			}));
			for (int i = 0; i != m_channels; ++i)
			{
				if (abs(m_mean_channels[i]) < abs(data[i]))
					m_mean_channels[i] = data[i];
			}
		}
		m_mean_time.push_back(total);
	}
	const float* GetTimeData(uint32_t* count)
	{
		*count = static_cast<uint32_t>(m_mean_time.size());
		return &m_mean_time[0];
	}
	const float* GetChannelData(uint32_t* count)
	{
		if (m_bCalcMean && !m_bChannelsDivided)
		{
			for (int i = 0; i != m_channels; ++i)
				m_mean_channels[i] /= m_mean_time.size();
			m_bChannelsDivided = true;
		}
		*count = m_channels;
		return &m_mean_channels[0];
	}
private:
	int m_channels;
	std::vector<float> m_mean_time;
	std::vector<float> m_mean_channels;
	bool m_bCalcMean;
	bool m_bChannelsDivided;
};

