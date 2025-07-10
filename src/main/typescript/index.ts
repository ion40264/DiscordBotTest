import axios from 'axios';

// --- 1. 取得するデータの型定義 ---
// APIから返されるユーザーデータの構造を定義します
interface ChatMessage {
	id: number;
	discordMessageId: string;
	quoteId: string;
	quoteDiscordId: string;
	name: string;
	message: string;
	attachmentUrlList: string[];
	createDate: string;
}

// --- 2. DOM要素の取得 ---
// データを表示するコンテナ要素を取得します
const chatMessageContainer = document.getElementById('chatMessageContainer') as HTMLElement;
const loadingMoreIndicator = document.getElementById('loadingMore') as HTMLElement; // 「さらに読み込み中...」表示用

let isLoading = false; // 現在データ読み込み中かどうか
let allDataLoaded = false; // 全てのデータが読み込まれたかどうか
let page = 0; // 読み込むページのオフセット、または開始位置（サーバーサイドのAPI設計による）
const size = 20; // 一度に取得するアイテム数（サーバーサイドのAPIと合わせる）


// --- 3. Axiosで全件データ取得とDOM操作を行う非同期関数 ---
async function fetchAndDisplayChatMessage(): Promise<void> {
	try {
		// Axiosを使って全ユーザーデータを取得します
		// レスポンスのデータ部分の型を `User[]` (Userオブジェクトの配列) として指定します
		const response = await axios.get<ChatMessage[]>(`/chat?page=${page}&size=${size}`)
			.then(function(response) {
				const chatMessages: ChatMessage[] = response.data; // 取得したデータ

				// 読み込み中のメッセージをクリアします
				if (page === 0) {
					chatMessageContainer.innerHTML = '';
				}
				// 取得したユーザーデータを元にDOMを操作します
				chatMessages.forEach(chatMessage => {
					// --- 4. DOM操作: ユーザーごとにカード要素を作成 ---
					const messageDiv = document.createElement('div');
					messageDiv.className = 'messageDiv';
					messageDiv.id = chatMessage.discordMessageId;

					const head = document.createElement('div');
					head.textContent = `id: ${chatMessage.id} 名前: ${chatMessage.name} ${chatMessage.createDate}`;
					head.className = 'headDiv';

					const main = document.createElement('div');
					main.className = 'mainDiv';
					const quate = document.createElement('div');
					quate.className = 'quateDiv';
					const message = document.createElement('div');
					if (chatMessage.quoteId != null) {
						quate.innerHTML = `>> <a href="#${chatMessage.quoteDiscordId}">${chatMessage.quoteId}</a>`;
						main.appendChild(quate);
					}
					let mainContents: string = '';
					mainContents += `${chatMessage.message}`;
					message.innerHTML = mainContents;
					main.appendChild(message);
					if (chatMessage.attachmentUrlList != null && chatMessage.attachmentUrlList.length != 0) {
						chatMessage.attachmentUrlList.forEach((url) => {
							const a = document.createElement('a');
							a.href = url;
							if (url.match("\.jpg|\.png|\.JPG|\.PNG") != null) {
								const img = document.createElement('img') as HTMLImageElement;
								img.src = url;
								img.width = 300;
								a.appendChild(img);
							} else {
								const p = document.createElement('p') as HTMLElement;
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
			});
	} catch (error) {
		// エラーハンドリング
		console.error('データの取得または表示中にエラーが発生しました:', error);
		chatMessageContainer.innerHTML = '<p>データの読み込みに失敗しました。</p>';

		// Axiosのエラーの場合、詳細情報を出力
		if (axios.isAxiosError(error) && error.response) {
			console.error('APIレスポンスエラー:', error.response.status, error.response.data);
		}
	}

}
// アプリケーション起動時にデータを取得して表示する関数を呼び出します
document.addEventListener('DOMContentLoaded', () => {
	fetchAndDisplayChatMessage();
});

async function onPostMessage(): Promise<void> {
	// FormDataオブジェクトを作成
	const formData: FormData = new FormData();

	const nameElement = document.getElementById('name') as HTMLInputElement;
	const name: string = nameElement.value as string;
	const messageElement = document.getElementById('message') as HTMLTextAreaElement;
	const message: string = messageElement.value as string;
	const referencedMessageIdElement = document.getElementById('referencedMessageId') as HTMLInputElement;
	const referencedMessageId: string = referencedMessageIdElement.value as string;
	const fileElement = document.getElementById('multipartFile') as HTMLInputElement;
	const multipartFileList: FileList = fileElement.files as FileList;
	if (multipartFileList && multipartFileList.length !== 0) {
		const multipartFile: File = multipartFileList[0];
		const fileName: string = multipartFile.name;
		formData.append('multipartFile', multipartFile);
		formData.append('fileName', fileName);
	}

	formData.append('name', name);
	formData.append('message', message);
	formData.append('referencedMessageId', referencedMessageId);

	// Axiosでリクエスト送信
	await axios.post('/chat', formData, {
	})
		.then(function(response) {
			resetChatMessageAndDisplay();
		});
}

let button = document.getElementById("button") as HTMLInputElement;
button.onclick = onPostMessage;

async function resetChatMessageAndDisplay(): Promise<void> {
	page = 0;
	fetchAndDisplayChatMessage();
}

setInterval(fetchAndDisplayChatMessage, 5000);

