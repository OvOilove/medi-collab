@echo off
chcp 65001 >nul
echo 正在停止所有 MediCollab 服务...
taskkill /f /fi "WINDOWTITLE eq medi-user*" >nul 2>&1
taskkill /f /fi "WINDOWTITLE eq medi-appointment*" >nul 2>&1
taskkill /f /fi "WINDOWTITLE eq medi-record*" >nul 2>&1
taskkill /f /fi "WINDOWTITLE eq medi-referral*" >nul 2>&1
taskkill /f /fi "WINDOWTITLE eq medi-gateway*" >nul 2>&1
taskkill /f /fi "WINDOWTITLE eq Nacos*" >nul 2>&1
echo 全部服务已停止
pause
