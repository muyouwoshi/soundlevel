APP_CPPFLAGS +=-std=c++11 #����ʹ��c++11�ĺ����ȹ���  
APP_STL := gnustl_static #GNU STL  
LOCAL_CPPFLAGS:=-std=c++11 -pthread
APP_CPPFLAGS := -fexceptions -frtti #�����쳣���ܣ�������ʱ����ʶ��  
APP_CPPFLAGS +=-fpermissive  #������Чʱ��ʾ���ɵı�����ʽ������û���õ��Ĵ������д���Ҳ����ͨ�����룻ʹ��GNU STLʱ���ô���std::string ��Ȼ���벻ͨ������
NDK_TOOLCHAIN_VERSION:=4.8
APP_ABI := all