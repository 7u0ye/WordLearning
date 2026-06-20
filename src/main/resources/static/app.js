const state = {
  token: localStorage.getItem("wordLearningToken") || "",
  authMode: "login",
  books: [],
  words: [],
  studyAnswerMap: {},
  studyIndex: 0,
  studyCorrect: 0,
  studyLocked: false,
  studyAdvanceTimer: null,
  exam: null,
  timerId: null,
  checkin: {
    checkedToday: false,
    checkedDays: [],
    consecutiveDays: 0,
    totalCheckinsThisMonth: 0
  }
};

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => Array.from(document.querySelectorAll(selector));

const dom = {
  appShell: $("#appShell"),
  authPanel: $("#authPanel"),
  dashboard: $("#dashboard"),
  authForm: $("#authForm"),
  authSubmit: $("#authSubmit"),
  authMessage: $("#authMessage"),
  emailField: $("#emailField"),
  usernameInput: $("#usernameInput"),
  passwordInput: $("#passwordInput"),
  emailInput: $("#emailInput"),
  sessionName: $("#sessionName"),
  logoutBtn: $("#logoutBtn"),
  bookSelect: $("#bookSelect"),
  studyCount: $("#studyCount"),
  loadStudyBtn: $("#loadStudyBtn"),
  saveStatusBtn: $("#saveStatusBtn"),
  wordGrid: $("#wordGrid"),
  examCount: $("#examCount"),
  examRatio: $("#examRatio"),
  ratioText: $("#ratioText"),
  examMinutes: $("#examMinutes"),
  startExamBtn: $("#startExamBtn"),
  resumeExamBtn: $("#resumeExamBtn"),
  examSetup: $("#examSetup"),
  examBoard: $("#examBoard"),
  examTitle: $("#examTitle"),
  examTimer: $("#examTimer"),
  questionList: $("#questionList"),
  submitExamBtn: $("#submitExamBtn"),
  examResult: $("#examResult"),
  refreshRecordsBtn: $("#refreshRecordsBtn"),
  recordList: $("#recordList"),
  toast: $("#toast"),
  checkinBtn: $("#checkinBtn"),
  consecutiveDays: $("#consecutiveDays"),
  monthlyDays: $("#monthlyDays"),
  checkinCalendar: $("#checkinCalendar")
};

function showToast(message) {
  dom.toast.textContent = message;
  dom.toast.classList.add("show");
  clearTimeout(showToast.timeout);
  showToast.timeout = setTimeout(() => dom.toast.classList.remove("show"), 2400);
}

function setBusy(button, busy, text) {
  if (!button) return;
  if (busy) {
    button.dataset.originalText = button.textContent;
    button.textContent = text || "处理中";
    button.disabled = true;
  } else {
    button.textContent = button.dataset.originalText || button.textContent;
    button.disabled = false;
  }
}

async function api(path, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };

  if (state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }

  const response = await fetch(path, {
    ...options,
    headers
  });

  const result = await response.json().catch(() => ({ code: 0, msg: "响应格式错误" }));
  if (!response.ok || result.code !== 1) {
    throw new Error(result.msg || "请求失败");
  }
  return result.data;
}

function setAuthenticated(isAuthenticated) {
  dom.authPanel.hidden = isAuthenticated;
  dom.appShell.hidden = !isAuthenticated;
  dom.dashboard.hidden = !isAuthenticated;
  dom.logoutBtn.hidden = !isAuthenticated;
  dom.sessionName.textContent = isAuthenticated ? "已登录" : "未登录";
}

function clearSession() {
  state.token = "";
  localStorage.removeItem("wordLearningToken");
  setAuthenticated(false);
  dom.wordGrid.innerHTML = "";
  dom.recordList.innerHTML = "";
  stopStudyAdvance();
  stopTimer();
}

function switchAuthMode(mode) {
  state.authMode = mode;
  $$(".mode-btn").forEach((button) => {
    button.classList.toggle("active", button.dataset.authMode === mode);
  });
  const isRegister = mode === "register";
  dom.emailField.hidden = !isRegister;
  dom.emailInput.required = isRegister;
  dom.authSubmit.textContent = isRegister ? "创建账号" : "登录";
  dom.authMessage.textContent = "";
}

async function handleAuth(event) {
  event.preventDefault();
  dom.authMessage.textContent = "";
  const username = dom.usernameInput.value.trim();
  const password = dom.passwordInput.value.trim();

  if (!username || !password) {
    dom.authMessage.textContent = "请输入用户名和密码";
    return;
  }

  const payload = { username, password };
  if (state.authMode === "register") {
    payload.email = dom.emailInput.value.trim();
  }

  setBusy(dom.authSubmit, true, state.authMode === "register" ? "创建中" : "登录中");
  try {
    if (state.authMode === "register") {
      await api("/api/user/register", {
        method: "POST",
        body: JSON.stringify(payload)
      });
    }

    const loginData = await api("/api/user/login", {
      method: "POST",
      body: JSON.stringify({ username, password })
    });
    state.token = loginData.token;
    localStorage.setItem("wordLearningToken", state.token);
    await bootAuthedApp();
    showToast("欢迎回来，今天也慢慢练稳");
  } catch (error) {
    dom.authMessage.textContent = error.message;
  } finally {
    setBusy(dom.authSubmit, false);
  }
}

async function bootAuthedApp() {
  setAuthenticated(true);
  await Promise.all([loadUser(), loadBooks(), loadRecords(), loadCheckinStatus()]);
}

async function loadUser() {
  try {
    const user = await api("/api/user/info");
    dom.sessionName.textContent = user.username || "已登录";
  } catch (error) {
    clearSession();
    showToast(error.message);
  }
}

async function loadBooks() {
  try {
    state.books = await api("/api/books");
    dom.bookSelect.innerHTML = "";
    if (!state.books.length) {
      dom.bookSelect.innerHTML = "<option value=\"\">暂无词书</option>";
      renderEmptyWords("还没有可用词书，请先在数据库中添加词书和单词。");
      return;
    }

    state.books.forEach((book) => {
      const option = document.createElement("option");
      option.value = book.id;
      option.textContent = `${book.name || "未命名词书"} · ${book.wordCount || 0} 词`;
      dom.bookSelect.appendChild(option);
    });
    renderEmptyWords("选择词书和数量后开始今天的学习。");
  } catch (error) {
    showToast(error.message);
  }
}

async function loadStudyWords() {
  const bookId = dom.bookSelect.value;
  const count = Number(dom.studyCount.value || 20);
  if (!bookId) {
    showToast("请先选择词书");
    return;
  }

  setBusy(dom.loadStudyBtn, true, "加载中");
  try {
    const [studyWords, bookWords] = await Promise.all([
      api(`/api/words/study?bookId=${encodeURIComponent(bookId)}&count=${count}`),
      api(`/api/books/${encodeURIComponent(bookId)}/words`)
    ]);
    state.words = studyWords || [];
    state.studyAnswerMap = buildStudyAnswerMap(bookWords || []);
    state.studyIndex = 0;
    state.studyCorrect = 0;
    state.studyLocked = false;
    renderWords(state.words);
    if (!state.words.length) {
      showToast("这本词书目前没有待学习单词");
    }
  } catch (error) {
    showToast(error.message);
  } finally {
    setBusy(dom.loadStudyBtn, false);
  }
}

function renderEmptyWords(message) {
  dom.saveStatusBtn.hidden = true;
  dom.wordGrid.classList.remove("study-single");
  dom.wordGrid.innerHTML = `<div class="empty-state">${message}</div>`;
}

function buildStudyAnswerMap(words) {
  return words.reduce((map, word) => {
    map[Number(word.id)] = word;
    return map;
  }, {});
}

function renderWords(words) {
  if (!words.length) {
    renderEmptyWords("今天的学习队列是空的。");
    return;
  }

  renderStudyQuestion();
}

function renderStudyQuestion() {
  stopStudyAdvance();
  state.studyLocked = false;

  const word = state.words[state.studyIndex];
  if (!word) {
    renderStudyComplete();
    return;
  }

  const wordId = word.wordId || word.id;
  const typeText = word.questionType === "cnToEn" ? "中文选英文" : "英文选中文";

  dom.saveStatusBtn.hidden = false;
  dom.saveStatusBtn.textContent = "跳过本题";
  dom.saveStatusBtn.disabled = false;
  dom.wordGrid.classList.add("study-single");
  dom.wordGrid.innerHTML = `
    <article class="word-card study-question-card" data-word-id="${wordId}">
      <div class="study-progress">
        <span>${state.studyIndex + 1} / ${state.words.length}</span>
        <strong>答对 ${state.studyCorrect}</strong>
      </div>
      <div class="word-head">
        <span class="question-type">${typeText}</span>
        <span>#${wordId}</span>
      </div>
      <strong class="study-question">${escapeHtml(word.question || "")}</strong>
      <div class="option-grid compact-options">
        ${(word.options || []).map((option) => `
          <button class="study-option" data-answer="${escapeAttribute(option)}" type="button">
            <span>${escapeHtml(option)}</span>
          </button>
        `).join("")}
      </div>
      <p class="study-feedback" id="studyFeedback" hidden></p>
    </article>
  `;

  $$(".study-option").forEach((button) => {
    button.addEventListener("click", () => answerStudyQuestion(button.dataset.answer));
  });
}

async function answerStudyQuestion(answer) {
  if (state.studyLocked) return;
  state.studyLocked = true;

  const word = state.words[state.studyIndex];
  const wordId = Number(word.wordId || word.id);
  const correctAnswer = getStudyCorrectAnswer(word);
  const isCorrect = normalizeAnswer(answer) === normalizeAnswer(correctAnswer);
  if (isCorrect) {
    state.studyCorrect += 1;
  }

  showStudyJudgement(answer, correctAnswer, isCorrect);
  try {
    await api("/api/words/batch-status", {
      method: "PUT",
      body: JSON.stringify({
        updates: [{ wordId, status: 0, answer }]
      })
    });
    state.studyAdvanceTimer = setTimeout(goNextStudyQuestion, 900);
  } catch (error) {
    showToast(error.message);
    state.studyLocked = false;
    $$(".study-option").forEach((button) => {
      button.disabled = false;
    });
  }
}

async function saveWordStatus() {
  await skipStudyQuestion();
}

async function skipStudyQuestion() {
  if (state.studyLocked || !state.words.length) return;
  state.studyLocked = true;

  const word = state.words[state.studyIndex];
  const wordId = Number(word.wordId || word.id);
  dom.saveStatusBtn.disabled = true;
  try {
    await api("/api/words/batch-status", {
      method: "PUT",
      body: JSON.stringify({
        updates: [{ wordId, status: 0, answer: "" }]
      })
    });
    goNextStudyQuestion();
  } catch (error) {
    showToast(error.message);
    state.studyLocked = false;
    dom.saveStatusBtn.disabled = false;
  }
}

function showStudyJudgement(answer, correctAnswer, isCorrect) {
  $$(".study-option").forEach((button) => {
    const option = button.dataset.answer;
    button.disabled = true;
    button.classList.toggle("selected", normalizeAnswer(option) === normalizeAnswer(answer));
    button.classList.toggle("correct", normalizeAnswer(option) === normalizeAnswer(correctAnswer));
    button.classList.toggle(
      "wrong",
      !isCorrect && normalizeAnswer(option) === normalizeAnswer(answer)
    );
  });

  const feedback = $("#studyFeedback");
  if (feedback) {
    feedback.hidden = false;
    feedback.className = `study-feedback ${isCorrect ? "correct" : "wrong"}`;
    feedback.textContent = isCorrect ? "答对了，进入下一题" : `答错了，正确答案：${correctAnswer || "未找到"}`;
  }
}

function goNextStudyQuestion() {
  stopStudyAdvance();
  state.studyIndex += 1;
  if (state.studyIndex >= state.words.length) {
    renderStudyComplete();
  } else {
    renderStudyQuestion();
  }
}

function renderStudyComplete() {
  dom.saveStatusBtn.hidden = true;
  dom.wordGrid.classList.remove("study-single");
  dom.wordGrid.innerHTML = `
    <div class="empty-state study-summary">
      <p class="eyebrow">SESSION COMPLETE</p>
      <h2>这一组练完了</h2>
      <p>答对 ${state.studyCorrect} / ${state.words.length} 题。</p>
    </div>
  `;
}

function getStudyCorrectAnswer(word) {
  const source = state.studyAnswerMap[Number(word.wordId || word.id)] || {};
  return word.questionType === "cnToEn" ? source.english : source.chinese;
}

function normalizeAnswer(value) {
  return String(value || "").trim().toLowerCase();
}

function stopStudyAdvance() {
  if (state.studyAdvanceTimer) {
    clearTimeout(state.studyAdvanceTimer);
    state.studyAdvanceTimer = null;
  }
}

function switchView(viewId) {
  $$(".nav-tab").forEach((tab) => tab.classList.toggle("active", tab.dataset.view === viewId));
  $$(".view").forEach((view) => view.classList.toggle("active", view.id === viewId));
  if (viewId === "recordsView") {
    loadRecords();
  }
}

function syncRatioText() {
  dom.ratioText.textContent = `${dom.examRatio.value}%`;
}

async function startExam() {
  const bookId = dom.bookSelect.value;
  if (!bookId) {
    showToast("请先选择词书");
    return;
  }

  const payload = {
    bookId: Number(bookId),
    count: Number(dom.examCount.value || 20),
    cnToEnRatio: Number(dom.examRatio.value || 50),
    durationMinutes: Number(dom.examMinutes.value || 20)
  };

  setBusy(dom.startExamBtn, true, "生成中");
  try {
    const exam = await api("/api/exam/start", {
      method: "POST",
      body: JSON.stringify(payload)
    });
    renderExam(exam);
    showToast("试卷已生成");
  } catch (error) {
    showToast(error.message);
  } finally {
    setBusy(dom.startExamBtn, false);
  }
}

async function resumeExam() {
  setBusy(dom.resumeExamBtn, true, "恢复中");
  try {
    const data = await api("/api/exam/current");
    if (!data) {
      showToast("没有进行中的测验");
      return;
    }
    renderExam(data.details);
  } catch (error) {
    showToast(error.message);
  } finally {
    setBusy(dom.resumeExamBtn, false);
  }
}

function renderExam(exam) {
  state.exam = exam;
  dom.examResult.hidden = true;
  dom.examResult.innerHTML = "";
  dom.examBoard.hidden = false;
  dom.examTitle.textContent = `测验 #${exam.examId}`;
  dom.questionList.innerHTML = exam.questions.map((question, index) => `
    <article class="question-card" data-detail-id="${question.detailId}">
      <p class="eyebrow">${question.questionType === "cnToEn" ? "中文选英文" : "英文选中文"} · ${index + 1}</p>
      <h3>${escapeHtml(question.question || "")}</h3>
      <div class="option-grid">
        ${(question.options || []).map((option) => {
          const checked = question.userAnswer === option ? "checked" : "";
          return `
            <label>
              <input type="radio" name="q-${question.detailId}" value="${escapeAttribute(option)}" ${checked}>
              <span>${escapeHtml(option)}</span>
            </label>
          `;
        }).join("")}
      </div>
    </article>
  `).join("");
  startTimer(Number(exam.durationMinutes || 20) * 60);
}

function startTimer(totalSeconds) {
  stopTimer();
  let remaining = totalSeconds;
  updateTimer(remaining);
  state.timerId = setInterval(() => {
    remaining -= 1;
    updateTimer(remaining);
    if (remaining <= 0) {
      stopTimer();
      showToast("时间到了，可以提交测验");
    }
  }, 1000);
}

function stopTimer() {
  if (state.timerId) {
    clearInterval(state.timerId);
    state.timerId = null;
  }
}

function updateTimer(seconds) {
  const safeSeconds = Math.max(0, seconds);
  const minutes = String(Math.floor(safeSeconds / 60)).padStart(2, "0");
  const rest = String(safeSeconds % 60).padStart(2, "0");
  dom.examTimer.textContent = `${minutes}:${rest}`;
}

async function submitExam() {
  if (!state.exam) {
    showToast("请先生成试卷");
    return;
  }

  const answers = $$(".question-card").map((card) => {
    const checked = card.querySelector("input:checked");
    return {
      detailId: Number(card.dataset.detailId),
      answer: checked ? checked.value : ""
    };
  });

  setBusy(dom.submitExamBtn, true, "提交中");
  try {
    const result = await api(`/api/exam/${state.exam.examId}/submit`, {
      method: "POST",
      body: JSON.stringify({ answers })
    });
    stopTimer();
    dom.examBoard.hidden = true;
    renderExamResult(result);
    await loadRecords();
  } catch (error) {
    showToast(error.message);
  } finally {
    setBusy(dom.submitExamBtn, false);
  }
}

function renderExamResult(result) {
  const score = Number(result.score || 0);
  dom.examResult.hidden = false;
  dom.examResult.innerHTML = `
    <p class="eyebrow">RESULT</p>
    <h2>${score.toFixed(1)} 分</h2>
    <p>答对 ${result.correctCount || 0} / ${result.totalCount || 0} 题。</p>
  `;
}

async function loadRecords() {
  if (!state.token) return;
  setBusy(dom.refreshRecordsBtn, true, "刷新中");
  try {
    const records = await api("/api/exam/records");
    renderRecords(records || []);
  } catch (error) {
    showToast(error.message);
  } finally {
    setBusy(dom.refreshRecordsBtn, false);
  }
}

function renderRecords(records) {
  if (!records.length) {
    dom.recordList.innerHTML = "<div class=\"empty-state\">还没有完成的测验记录。</div>";
    return;
  }

  dom.recordList.innerHTML = records.map((record) => {
    const score = Number(record.score || 0);
    return `
      <article class="record-card">
        <div>
          <strong>测验 #${record.id}</strong>
          <span>${formatDate(record.createdAt)} · ${record.correctCount || 0}/${record.totalCount || 0} 题 · ${formatDuration(record.durationSeconds)}</span>
        </div>
        <div class="score-badge">${score.toFixed(0)}</div>
      </article>
    `;
  }).join("");
}

function formatDate(value) {
  if (!value) return "未知时间";
  return String(value).replace("T", " ").slice(0, 16);
}

function formatDuration(seconds) {
  if (!seconds) return "未记录时长";
  return `${Math.round(seconds / 60)} 分钟`;
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#039;");
}

function escapeAttribute(value) {
  return escapeHtml(value).replaceAll("`", "&#096;");
}

function bindEvents() {
  $$(".mode-btn").forEach((button) => {
    button.addEventListener("click", () => switchAuthMode(button.dataset.authMode));
  });

  $$(".nav-tab").forEach((button) => {
    button.addEventListener("click", () => switchView(button.dataset.view));
  });

  dom.authForm.addEventListener("submit", handleAuth);
  dom.logoutBtn.addEventListener("click", clearSession);
  dom.loadStudyBtn.addEventListener("click", loadStudyWords);
  dom.saveStatusBtn.addEventListener("click", saveWordStatus);
  dom.examRatio.addEventListener("input", syncRatioText);
  dom.startExamBtn.addEventListener("click", startExam);
  dom.resumeExamBtn.addEventListener("click", resumeExam);
  dom.submitExamBtn.addEventListener("click", submitExam);
  dom.refreshRecordsBtn.addEventListener("click", loadRecords);
  dom.checkinBtn.addEventListener("click", doCheckin);
}

// ── 每日打卡 ──────────────────────────────────────────

async function loadCheckinStatus() {
  if (!state.token) return;
  try {
    const data = await api("/api/checkin/status");
    state.checkin = data;
    renderCheckin();
  } catch (error) {
    // 打卡功能非核心，静默失败
  }
}

async function doCheckin() {
  setBusy(dom.checkinBtn, true, "打卡中");
  try {
    await api("/api/checkin", { method: "POST" });
    await loadCheckinStatus();
    showToast("打卡成功！");
  } catch (error) {
    showToast(error.message);
  } finally {
    setBusy(dom.checkinBtn, false);
  }
}

function renderCheckin() {
  const { checkedToday, checkedDays, consecutiveDays, totalCheckinsThisMonth } = state.checkin;

  // 更新统计数字
  dom.consecutiveDays.textContent = consecutiveDays;
  dom.monthlyDays.textContent = totalCheckinsThisMonth;

  // 更新按钮状态
  if (checkedToday) {
    dom.checkinBtn.textContent = "已打卡 ✓";
    dom.checkinBtn.disabled = true;
    dom.checkinBtn.style.opacity = "0.65";
  } else {
    dom.checkinBtn.textContent = "今日打卡";
    dom.checkinBtn.disabled = false;
    dom.checkinBtn.style.opacity = "1";
  }

  // 渲染日历
  renderCalendar(checkedDays || []);
}

// LeetCode 风格热力图：列 = 周，行 = 星期几（一 ~ 日）
function renderCalendar(checkedDays) {
  var now = new Date();
  var year = now.getFullYear();
  var month = now.getMonth();       // 0-based
  var today = now.getDate();

  var firstDayOfWeek = new Date(year, month, 1).getDay(); // 0=Sun
  var daysInMonth = new Date(year, month + 1, 0).getDate();

  // 周一为第 0 行，周日为第 6 行
  var startRow = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1;
  var numCols = Math.ceil((startRow + daysInMonth) / 7);
  var checkedSet = new Set(checkedDays || []);

  // 行标签
  var rowLabels = ["一", "二", "三", "四", "五", "六", "日"];

  // 构建 7 行 × N 列的网格数据
  var grid = [];
  for (var r = 0; r < 7; r++) {
    grid[r] = [];
    for (var c = 0; c < numCols; c++) {
      grid[r][c] = -1; // -1 = 空格子
    }
  }

  var day = 1;
  for (var c = 0; c < numCols; c++) {
    var startR = (c === 0) ? startRow : 0;
    for (var r = startR; r < 7 && day <= daysInMonth; r++) {
      grid[r][c] = day;
      day++;
    }
  }

  // 组装 HTML
  var monthNames = ["1月", "2月", "3月", "4月", "5月", "6月",
                    "7月", "8月", "9月", "10月", "11月", "12月"];

  var html = "";
  // 顶部月份标签（放在第一列上方）
  html += '<div class="checkin-graph-header">';
  html += '<span class="checkin-graph-month">' + monthNames[month] + "</span>";
  html += "</div>";

  // 主体：左行标签 + 右列
  html += '<div class="checkin-graph-body">';

  // 左侧行标签
  html += '<div class="checkin-row-labels">';
  for (var r2 = 0; r2 < 7; r2++) {
    html += "<span>" + rowLabels[r2] + "</span>";
  }
  html += "</div>";

  // 右侧列
  html += '<div class="checkin-columns">';
  for (var c2 = 0; c2 < numCols; c2++) {
    html += '<div class="checkin-column">';
    for (var r3 = 0; r3 < 7; r3++) {
      var dayNum = grid[r3][c2];
      if (dayNum === -1) {
        html += '<div class="checkin-cell empty"></div>';
      } else {
        var cls = "checkin-cell";
        if (checkedSet.has(dayNum)) {
          cls += " checked";
        }
        if (dayNum === today) {
          cls += " today";
        }
        var title = (month + 1) + "月" + dayNum + "日";
        html += '<div class="' + cls + '" title="' + title + '"></div>';
      }
    }
    html += "</div>";
  }
  html += "</div>"; // .checkin-columns
  html += "</div>"; // .checkin-graph-body

  dom.checkinCalendar.innerHTML = html;
}

// ─────────────────────────────────────────────────────

async function init() {
  bindEvents();
  syncRatioText();
  switchAuthMode("login");
  if (state.token) {
    try {
      await bootAuthedApp();
    } catch (error) {
      clearSession();
    }
  } else {
    setAuthenticated(false);
  }
}

init();
