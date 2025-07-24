import axios from "axios";

const chatMessageContainer = document.getElementById(
	"chatMessageContainer"
) as HTMLDivElement;
const loadingMoreIndicator = document.getElementById(
	"loading-indicator"
) as HTMLDivElement; // 「さらに読み込み中...」表示用
const popupbox = document.getElementById("popup") as HTMLDivElement; // 「さらに読み込み中...」表示用

let isLoading = false; // 現在データ読み込み中かどうか
let allDataLoaded = false; // 全てのデータが読み込まれたかどうか
let page = 0; // 読み込むページのオフセット、または開始位置（サーバーサイドのAPI設計による）
const size = 100; // 一度に取得するアイテム数（サーバーサイドのAPIと合わせる）
let maxSize = 0;

window.addEventListener("DOMContentLoaded", (event: Event) => {
	const path: string = window.location.pathname;

	if (path.includes("index")) {
		initIndex();
	} else if (path.includes("chatHtml")) {
		initChat();
	} else if (path.includes("memberHtml")) {
		initMember();
	} else {
		// その他のページ用の共通処理
	}
});

function initIndex() { }

function initChat() {
	// アプリケーション起動時にデータを取得して表示する関数を呼び出します
	fetchAndDisplayChatMessage(false);
	const button = document.getElementById("button") as HTMLInputElement;
	button.onclick = onPostMessage;
	//	setInterval(resetAndDisplayChatMessage, 5000);
}

function initMember() {
	fetchAndDisplayMember();
	const button = document.getElementById("deleteButton") as 	HTMLInputElement;
	button.onclick = onDeleteMember;
const addButton = document.getElementById("addButton") as 	HTMLInputElement;
addButton.onclick = onDeleteMember;
}
async function onDeleteMember(): Promise <void> {
	const deleteInput = document.getElementById("deleteInput") as 	HTMLInputElement;
	const memberIdToDelete: number = parseInt(deleteInput.value, 10); 
	await axios.delete(`/member/` + memberIdToDelete);
	fetchAndDisplayMember();
}
async function onAddMember(): Promise<void> {
	const ayarabuInput = document.getElementById("ayarabuInput") as HTMLInputElement; // 変数名をdeleteInputからayarabuInputに修正
	if (!ayarabuInput) { // 要素が存在しない場合のエラーを避ける
		console.error("ayarabuInput element not found.");
		return;
	}
	const memberData: AllianceMemberForm = {
		id:  -1, // 新規追加のため仮のID
		memberRole: "MEMBER",
		discordMemberId: "",
		discordName:"",
		ayarabuId: "",
		ayarabuName: ayarabuInput.value,
		alliance: "NONE",
		statementCount : 0,
		createDate: "",
		isBot: 0
	};
	await axios.post(`/member`, memberData);
	fetchAndDisplayMember();
}
interface AllianceMemberForm {
	id: number;
	memberRole: string;
	discordMemberId: string;
	discordName: string;
	ayarabuId: string;
	ayarabuName: string;
	alliance: string;
	statementCount: number;
	createDate: string;
	isBot: number;
}


// --- 1. 取得するデータの型定義 ---
// APIから返されるユーザーデータの構造を定義します
interface ChatMessage {
	id: number;
	discordMessageId: string;
	quoteId: string;
	quoteDiscordId: string;
	name: string;
	message: string;
	chatAttachmentDtoList: ChatAttachmentDto[];
	createDate: string;
}

interface ChatAttachmentDto {
	attachmentUrl : string;
	attachmentFileName : string;
}

interface MessageSize {
	size: number;
}

// --- 2. DOM要素の取得 ---
// データを表示するコンテナ要素を取得します

// --- 3. Axiosで全件データ取得とDOM操作を行う非同期関数 ---
async function fetchAndDisplayChatMessage(
	resetFlag: boolean = false
): Promise<void> {
	//	const messageSizeResponse = await axios.get<MessageSize>(`/chat/size`);
	//	maxSize = messageSizeResponse.data.size;
	// Axiosを使って全ユーザーデータを取得します
	// レスポンスのデータ部分の型を `User[]` (Userオブジェクトの配列) として指定します
	if (page === 0 || resetFlag) {
		page = 0;
		chatMessageContainer.innerHTML = "";

		loadingMoreIndicator.innerHTML = "ロード中...";
	}
	const response = await axios.get<ChatMessage[]>(
		`/chat/pageable?page=${page}&size=${size}`
	);
	const chatMessages: ChatMessage[] = response.data; // 取得したデータ

	const loading = document.createElement("div");
	// 読み込み中のメッセージをクリアします
	// 取得したユーザーデータを元にDOMを操作します
	chatMessages.forEach((chatMessage) => {
		// --- 4. DOM操作: ユーザーごとにカード要素を作成 ---
		const messageDiv = document.createElement("div") as HTMLDivElement;
		messageDiv.className = "messageDiv";
		messageDiv.id = chatMessage.discordMessageId;

		const head = document.createElement("button") as HTMLButtonElement;
		head.addEventListener("click", onHeadClick);
		const idElement = document.createElement("div") as HTMLDivElement;
		head.appendChild(idElement);
		head.textContent = `id: ${chatMessage.id} 名前: ${chatMessage.name} ${chatMessage.createDate}`;
		head.className = "headDiv";
		head.id = `${chatMessage.id}`;

		const main = document.createElement("div") as HTMLDivElement;
		main.className = "mainDiv";
		const quate = document.createElement("div") as HTMLDivElement;
		quate.className = "quateDiv";
		const message = document.createElement("div") as HTMLDivElement;
		if (chatMessage.quoteId != null) {
			quate.innerHTML = `>> <a href="#${chatMessage.quoteDiscordId}">${chatMessage.quoteId}</a>`;
			main.appendChild(quate);
		}
		let mainContents: string = "";
		mainContents += `${chatMessage.message}`;
		message.innerHTML = mainContents;
		main.appendChild(message);
		if (
			chatMessage.chatAttachmentDtoList != null &&
			chatMessage.chatAttachmentDtoList.length != 0
		) {
			chatMessage.chatAttachmentDtoList.forEach((chatAttachmentDto) => {
				const a = document.createElement("a");
				a.href = chatAttachmentDto.attachmentUrl;
				if (chatAttachmentDto.attachmentUrl.match(".jpg|.png|.JPG|.PNG") != null) {
					const img = document.createElement("img") as HTMLImageElement;
					img.src = chatAttachmentDto.attachmentUrl;
					img.width = 300;
					a.appendChild(img);
				} else {
					const p = document.createElement("p") as HTMLElement;
					p.textContent = "リンク";
					a.appendChild(p);
				}
				main.appendChild(a);
			});
		}

		// 作成した要素をユーザーカードに追加
		messageDiv.appendChild(head);
		messageDiv.appendChild(main);
		// ユーザーカードをコンテナに追加
		chatMessageContainer.appendChild(messageDiv);
		page++;
	});
	loadingMoreIndicator.innerHTML = "";
}

async function onPostMessage(): Promise<void> {
	// FormDataオブジェクトを作成
	const formData: FormData = new FormData();

	const nameElement = document.getElementById("name") as HTMLInputElement;
	const name: string = nameElement.value as string;
	const messageElement = document.getElementById(
		"message"
	) as HTMLTextAreaElement;
	const message: string = messageElement.value as string;
	const referencedMessageIdElement = document.getElementById(
		"referencedMessageId"
	) as HTMLInputElement;
	const referencedMessageId: string =
		referencedMessageIdElement.value as string;
	const fileElement = document.getElementById(
		"multipartFile"
	) as HTMLInputElement;
	const multipartFileList: FileList = fileElement.files as FileList;
	if (multipartFileList && multipartFileList.length !== 0) {
		const multipartFile: File = multipartFileList[0];
		const fileName: string = multipartFile.name;
		formData.append("multipartFile", multipartFile);
		formData.append("fileName", fileName);
	}

	formData.append("name", name);
	formData.append("message", message);
	formData.append("referencedMessageId", referencedMessageId);

	// Axiosでリクエスト送信
	await axios.post("/chat", formData, {}).then(function(response) {
		window.setTimeout(resetAndDisplayChatMessage, 500);
	});
}

function onHeadClick(event: MouseEvent): void {
	const clickedElement = event.target as HTMLElement;
	const referencedMessageIdElement = document.getElementById(
		"referencedMessageId"
	) as HTMLInputElement;
	const headElement: HTMLInputElement = document.getElementById(
		clickedElement.id
	) as HTMLInputElement;
	referencedMessageIdElement.value = clickedElement.id;
	headElement.style.display = "block";
}

async function checkNewMessage(): Promise<void> {
	const messageSizeResponse = await axios.get<MessageSize>(`/chat/size`);
	const newSize = messageSizeResponse.data.size;
	if (newSize > maxSize) {
		resetAndDisplayChatMessage();
	}
}

async function resetAndDisplayChatMessage(): Promise<void> {
	page = 0;
	fetchAndDisplayChatMessage(true);
}

// メンバー用ソース
interface Member {
	id: number;
	memberRole: string;
	discordMemberId: string;
	discordName: string;
	ayarabuId: string;
	ayarabuName: string;
	alliance: string;
	statementCount: number;
	createDate: string;
}
async function fetchAndDisplayMember(): Promise<void> {
	const response = await axios.get<Member[]>(`/member`);
	const members: Member[] = response.data; // 取得したデータ
	const memberRoles: string[] = ["LEADER", "SUB_LEADER", "MEMBER"];
	const memberAlliance: string[] = ["HOKKORI", "HONTO_HOKKORI", "NONE"];

	const memberTable = document.querySelector(
		"#memberTable tbody"
	) as HTMLElement;
	memberTable.innerHTML = "";
	members.forEach((member) => {
		const row = document.createElement("tr") as HTMLTableRowElement;
		// セルのidを設定して、更新時にどの行のデータか識別できるようにする
		row.dataset.id = member.id + "";
		const roleOptions = memberRoles
			.map(
				(role) =>
					`<option value="${role}" ${member.memberRole === role ? "selected" : ""
					}>${role}</option>`
			)
			.join("");
		const allianceOptions = memberAlliance
			.map(
				(alliance) =>
					`<option value="${alliance}" ${member.alliance === alliance ? "selected" : ""
					}>${alliance}</option>`
			)
			.join("");
		row.innerHTML = `
	                    <td>${member.id}</td>
						<td><select class="memberRoleSelect">${roleOptions}</select></td>
						<td>${member.discordMemberId}</td>
						<td>${member.discordName}</td>
						<td contenteditable="true">${member.ayarabuId}</td>
						<td contenteditable="true">${member.ayarabuName}</td>
						<td><select class="memberAllianceSelect">${allianceOptions}</select></td>
	                    <td>${member.statementCount}</td>
						<td>${member.createDate}</td>
	                `;

		memberTable.appendChild(row);

		// 変更を検知してPUTリクエストを送信
		const doc = document.getElementById("memberTable") as HTMLElement;
		doc.addEventListener("focusout", async (event) => {
			// 汎用的なHTMLElementとして扱う
			const targetElement = event.target as HTMLElement;

			// 編集可能なセルからフォーカスが外れたことを確認
			if (
				targetElement.tagName === "TD" &&
				targetElement.getAttribute("contenteditable") === "true"
			) {
				const targetCell = targetElement as HTMLTableCellElement;
				const row = targetCell.closest("tr") as HTMLTableRowElement;
				const rowId: string = row.dataset.id + "";

				const updateMember: Member = {
					id: parseInt(rowId),
					memberRole: row.cells[1].textContent || "",
					discordMemberId: row.cells[2].textContent || "",
					discordName: row.cells[3].textContent || "",
					ayarabuId: row.cells[4].textContent || "",
					ayarabuName: row.cells[5].textContent || "",
					alliance: row.cells[6].textContent || "",
					statementCount: parseInt(row.cells[7].textContent || "0", 10),
					createDate: row.cells[8].textContent || "",
				};

				console.log("updateMEmber:", updateMember);
				await axios.put<Member>(`/member`, updateMember, {});
			} else if (targetElement.className === "memberRoleSelect") {
				const selectElement = targetElement as HTMLSelectElement;
				const row = selectElement.closest("tr") as HTMLTableRowElement;
				const rowId: string = row.dataset.id + "";

				const updateMember: Member = {
					id: parseInt(rowId),
					memberRole: selectElement.value,
					discordMemberId: row.cells[2].textContent || "",
					discordName: row.cells[3].textContent || "",
					ayarabuId: row.cells[4].textContent || "",
					ayarabuName: row.cells[5].textContent || "",
					alliance: row.cells[5].textContent || "",
					statementCount: parseInt(row.cells[7].textContent || "0", 10),
					createDate: row.cells[8].textContent || "",
				};

				console.log("updateMEmber:", updateMember);
				await axios.put<Member>(`/member`, updateMember, {});
			} else if (targetElement.className === "memberAllianceSelect") {
				const selectElement = targetElement as HTMLSelectElement;
				const row = selectElement.closest("tr") as HTMLTableRowElement;
				const rowId: string = row.dataset.id + "";

				const updateMember: Member = {
					id: parseInt(rowId),
					memberRole: selectElement.value,
					discordMemberId: row.cells[2].textContent || "",
					discordName: row.cells[3].textContent || "",
					ayarabuId: row.cells[4].textContent || "",
					ayarabuName: row.cells[5].textContent || "",
					alliance: selectElement.value,
					statementCount: parseInt(row.cells[7].textContent || "0", 10),
					createDate: row.cells[8].textContent || "",
				};

				console.log("updateMEmber:", updateMember);
				await axios.put<Member>(`/member`, updateMember, {});
			}
		});
	});
}
