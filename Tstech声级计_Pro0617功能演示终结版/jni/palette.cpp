#include "palette.h"

static bool g_bPaletteInited = false;

static uint32_t g_BlackWhitePalette[256];
static uint32_t g_FarbPalette[256];
#undef RGB
#define RGB(r, g, b) (0xFF000000 | ((r)<< 16) | ((g)<<8) | (b))

void InitPalette()
{
	int		nPalSize = 256;
	int		n = nPalSize / 4;
	float	fPalSize = float(nPalSize - 1);
	float	fn = float(nPalSize / 4);
	for (int i = 0; i < nPalSize; i++)
	{
		g_BlackWhitePalette[i] = RGB(i, i, i);
	}

	for (int i = 0; i < n / 2; i++)
	{
		int r, g, b;
		r = 0;
		g = 0;
		b = int((fn / 2 + i) / fn * fPalSize + 0.5);
		g_FarbPalette[i] = RGB(r, g, b);
	}

	for (int i = 0; i < n; i++)
	{
		int r, g, b;
		r = 0;
		g = int(i / fn * fPalSize + 0.5);
		b = nPalSize - 1;
		g_FarbPalette[i + n / 2] = RGB(r, g, b);
	}
	for (int i = 0; i < n; i++)
	{
		int r, g, b;
		r = int(i / fn * fPalSize);
		g = nPalSize - 1;
		b = int(((fn - i) / fn) * fPalSize + 0.5);
		g_FarbPalette[i + n / 2 + n] = RGB(r, g, b);
	}
	for (int i = 0; i < n; i++)
	{
		int r, g, b;
		r= nPalSize - 1;
		g = int(((fn - i) / fn) * fPalSize + 0.5);
		b = 0;
		g_FarbPalette[i + n / 2 + 2 * n] = RGB(r, g, b);
	}
	for (int i = 0; i < n / 2; i++)
	{
		int r, g, b;
		r = int(((fn - i) / fn) * fPalSize + 0.5);
		g = 0;
		b = 0;
		g_FarbPalette[i + n / 2 + 3 * n] = RGB(r, g, b);
	}
}

const uint32_t* GetPalette(int nID)
{
	if (!g_bPaletteInited)
	{
		InitPalette();
		g_bPaletteInited = true;
	}
	switch (nID)
	{
	case 0:
		return g_BlackWhitePalette;
		break;
	case 1:
		return g_FarbPalette;
		break;
	default:
		return nullptr;
		break;
	}
}

