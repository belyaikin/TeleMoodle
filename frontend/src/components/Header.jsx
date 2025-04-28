import '../styles/header.css'

export default function Header() {
    return(
        <div className="header-component">
            <h1>TeleMoodle</h1>
            <ul className='header-list'>
                <li>Your name</li>
                <li>Support</li>
            </ul>
        </div>
    )
}